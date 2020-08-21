/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *
 * 	  This file is part of Gaia.
 *
 *    Gaia is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Gaia is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Gaia.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.primordialmoros.gaia.implementation;

import com.github.primordialmoros.gaia.AbstractArenaManager;
import com.github.primordialmoros.gaia.Gaia;
import com.github.primordialmoros.gaia.api.Arena;
import com.github.primordialmoros.gaia.api.GaiaChunk;
import com.github.primordialmoros.gaia.api.GaiaRegion;
import com.github.primordialmoros.gaia.api.GaiaVector;
import com.github.primordialmoros.gaia.io.GaiaIO;
import com.github.primordialmoros.gaia.platform.GaiaPlayer;
import com.github.primordialmoros.gaia.platform.PlayerWrapper;
import com.github.primordialmoros.gaia.platform.WorldWrapper;
import com.github.primordialmoros.gaia.util.functional.GaiaConsumerInfo;
import com.github.primordialmoros.gaia.util.metadata.ArenaMetadata;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ArenaManager extends AbstractArenaManager {
	@Override
	public void revertArena(final Arena arena, final GaiaConsumerInfo info) {
		arena.setReverting(true);
		arena.getSubRegions().forEach(gcr -> PaperGaiaChunk.revertChunk(gcr, arena.getWorld()));
		Bukkit.getScheduler().runTaskTimer(Gaia.getPlugin(), l -> {
			if (!arena.isReverting()) {
				info.sender.sendMessage(TextComponent.of("Cancelled reverting ", NamedTextColor.RED).append(arena.getFormattedName()));
				l.cancel();
			} else {
				if (arena.getSubRegions().stream().noneMatch(GaiaChunk::isReverting)) {
					final long deltaTime = System.currentTimeMillis() - info.startTime;
					info.sender.sendMessage(TextComponent.builder("Finished reverting ", NamedTextColor.GREEN)
						.append(arena.getFormattedName()).append(" (" + deltaTime + "ms).", NamedTextColor.GREEN).build()
					);
					arena.setReverting(false);
					l.cancel();
				}
			}
		}, 1, 1);
	}

	@Override
	public boolean createArena(final GaiaPlayer sender, final String arenaName) {
		final Region r;
		final Player player = ((PlayerWrapper) sender).get();
		final WorldWrapper world = new WorldWrapper(player.getWorld());
		try {
			r = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player)).getSelection(BukkitAdapter.adapt(world.get()));
		} catch (IncompleteRegionException e) {
			sender.sendBrandedMessage("You need a WorldEdit selection in order to create an arena.", NamedTextColor.RED);
			return false;
		}

		if (!(r instanceof CuboidRegion)) {
			sender.sendBrandedMessage("Only cuboid regions are allowed. Aborting arena creation.", NamedTextColor.RED);
			return false;
		}
		int radius = Math.max(Math.max(r.getLength(), r.getWidth()), Math.max(r.getHeight(), 64));
		if (radius > 512) { // For safety reasons
			sender.sendBrandedMessage("Regions can't be larger than 32 chunks in any dimension.", NamedTextColor.RED);
			return false;
		}
		if (r.getCenter().distanceSq(BukkitAdapter.adapt(player.getLocation()).toVector()) > radius * radius) {
			sender.sendBrandedMessage("You are standing too far away from the selected region's center. Aborting arena creation.", NamedTextColor.RED);
			return false;
		}

		final GaiaVector min = GaiaVector.at(r.getMinimumPoint().getX(), r.getMinimumPoint().getY(), r.getMinimumPoint().getZ());
		final GaiaVector max = GaiaVector.at(r.getMaximumPoint().getX(), r.getMaximumPoint().getY(), r.getMaximumPoint().getZ());
		final GaiaRegion gr = new GaiaRegion(min, max);

		if (!isUniqueRegion(world.getUID(), gr)) {
			sender.sendBrandedMessage("Selected region intersects with another arena, Aborting arena creation.", NamedTextColor.RED);
			return false;
		}
		final Arena arena = new Arena(arenaName, world, gr);
		if (!GaiaIO.getInstance().createArenaFiles(arenaName)) {
			sender.sendBrandedMessage("Critical error, could not create arena file, check console for more info.", NamedTextColor.RED);
			return false;
		}
		sender.sendBrandedMessage(TextComponent.of("Analyzing ", NamedTextColor.GREEN).append(arena.getFormattedName()));
		final GaiaConsumerInfo info = new GaiaConsumerInfo(sender);
		final TextComponent fail = TextComponent.builder("Something went wrong, couldn't create arena: ", NamedTextColor.RED)
			.append(arena.getFormattedName()).build();
		final TextComponent success = arena.getFormattedName().append(TextComponent.of(" has been created!", NamedTextColor.GREEN));
		if (!splitIntoChunks(arena)) {
			sender.sendBrandedMessage(fail);
			return false;
		}
		arena.setMetadata(new ArenaMetadata(arena));
		final long timeout = Gaia.getPlugin().getConfig().getLong("Analysis.Timeout");
		Bukkit.getScheduler().runTaskTimer(Gaia.getPlugin(), l -> {
			if (System.currentTimeMillis() > info.startTime + timeout) {
				sender.sendBrandedMessage(fail);
				removeArena(arena.getName());
				l.cancel();
			} else {
				if (arena.getSubRegions().stream().allMatch(GaiaChunk::isAnalyzed) && arena.finalizeArena()) {
					Bukkit.getScheduler().runTaskAsynchronously(Gaia.getPlugin(), () -> {
						boolean result = GaiaIO.getInstance().saveArena((ArenaMetadata) arena.getMetadata());
						sender.sendBrandedMessage(result ? success : fail);
					});
					l.cancel();
				}
			}
		}, 1, 1);
		addArena(arena);
		return true;
	}

	public static boolean splitIntoChunks(final Arena arena) {
		final int minX = arena.getRegion().getMinimumPoint().getX();
		final int maxX = arena.getRegion().getMaximumPoint().getX();
		final int minY = arena.getRegion().getMinimumPoint().getY();
		final int maxY = arena.getRegion().getMaximumPoint().getY();
		final int minZ = arena.getRegion().getMinimumPoint().getZ();
		final int maxZ = arena.getRegion().getMaximumPoint().getZ();

		int tempX, tempZ;
		GaiaVector v1, v2;
		for (int x = minX >> 4; x <= maxX >> 4; ++x) {
			tempX = x * 16;
			for (int z = minZ >> 4; z <= maxZ >> 4; ++z) {
				tempZ = z * 16;
				v1 = GaiaVector.atXZClamped(tempX, minY, tempZ, minX, maxX, minZ, maxZ);
				v2 = GaiaVector.atXZClamped(tempX + 15, maxY, tempZ + 15, minX, maxX, minZ, maxZ);
				final PaperGaiaChunk chunkRegion = Gaia.getPlugin().getChunkFactory().create(UUID.randomUUID(), arena, new GaiaRegion(v1, v2));
				PaperGaiaChunk.analyzeChunk(chunkRegion, arena.getWorld());
			}
		}
		return !arena.getSubRegions().isEmpty();
	}
}
