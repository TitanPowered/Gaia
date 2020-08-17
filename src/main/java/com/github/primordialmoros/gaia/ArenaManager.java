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

package com.github.primordialmoros.gaia;

import com.github.primordialmoros.gaia.io.GaiaIO;
import com.github.primordialmoros.gaia.methods.CoreMethods;
import com.github.primordialmoros.gaia.util.GaiaChunkRegion;
import com.github.primordialmoros.gaia.util.GaiaRegion;
import com.github.primordialmoros.gaia.util.GaiaVector;
import com.github.primordialmoros.gaia.util.functional.GaiaConsumerInfo;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ArenaManager {

	private static final Map<String, Arena> ARENAS = new ConcurrentHashMap<>();

	public static Arena getArena(final String name) {
		return ARENAS.get(name);
	}

	public static boolean arenaExists(final String name) {
		return ARENAS.containsKey(name) || GaiaIO.getInstance().arenaFileExists(name);
	}

	public static List<String> getSortedArenaNames() {
		return ARENAS.keySet().stream().sorted().collect(Collectors.toList());
	}

	public static Collection<Arena> getAllArenas() {
		return ARENAS.values();
	}

	public static int getArenaCount() {
		return ARENAS.size();
	}

	public static void addArena(final Arena arena) {
		if (arena != null && !ARENAS.containsKey(arena.getName())) ARENAS.put(arena.getName(), arena);
	}

	public static void removeArena(final String name) {
		ARENAS.remove(name);
		GaiaIO.getInstance().deleteArena(name); // Cleanup files
	}

	public static void cancelRevertArena(final Arena arena) {
		arena.getSubRegions().forEach(GaiaChunkRegion::cancelReverting);
	}

	public static void revertArena(final Arena arena, final GaiaConsumerInfo info) {
		arena.getSubRegions().forEach(gcr -> GaiaChunkRegion.revertChunk(gcr, arena.getWorld()));
		Bukkit.getScheduler().runTaskTimer(Gaia.getPlugin(), l -> {
			if (!arena.isReverting()) {
				CoreMethods.sendMessage(info.sender, info.fail);
				l.cancel();
			} else {
				if (arena.getSubRegions().stream().noneMatch(GaiaChunkRegion::isReverting)) {
					arena.setReverting(false);
					final long deltaTime = System.currentTimeMillis() - info.startTime;
					CoreMethods.sendMessage(info.sender, info.success + ChatColor.GREEN + " (" + deltaTime + "ms).");
					l.cancel();
				}
			}
		}, 1, 1);
	}

	public static Optional<Arena> getArenaAtPoint(final UUID id, final GaiaVector l) {
		return getAllArenas().stream().filter(a -> a.getWorldUID().equals(id)).filter(arena -> arena.getRegion().contains(l)).findAny();
	}

	public static boolean isUniqueRegion(final UUID id, final GaiaRegion rg) {
		return ARENAS.values().stream().filter(a -> a.getWorldUID().equals(id)).map(Arena::getRegion).noneMatch(rg::intersects);
	}

	public static boolean createArena(final Player player, final String arenaName, final World world) {
		try {
			final Region r = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player)).getSelection(BukkitAdapter.adapt(world));
			int radius = Math.max(Math.max(r.getLength(), r.getWidth()), Math.max(r.getHeight(), 64));
			if (radius > 512) { // For safety reasons
				CoreMethods.sendMessage(player, Gaia.PREFIX + ChatColor.RED + "Regions can't be larger than 32 chunks in any dimension.");
				return false;
			}
			if (r.getCenter().distanceSq(BukkitAdapter.adapt(player.getLocation()).toVector()) > radius * radius) {
				CoreMethods.sendMessage(player, Gaia.PREFIX + ChatColor.RED + "You are standing too far away from the selected region's center. Aborting arena creation.");
				return false;
			}
			if (!(r instanceof CuboidRegion)) {
				CoreMethods.sendMessage(player, Gaia.PREFIX + ChatColor.RED + "Only cuboid regions are allowed. Aborting arena creation.");
				return false;
			}
			final GaiaVector min = GaiaVector.at(r.getMinimumPoint().getX(), r.getMinimumPoint().getY(), r.getMinimumPoint().getZ());
			final GaiaVector max = GaiaVector.at(r.getMaximumPoint().getX(), r.getMaximumPoint().getY(), r.getMaximumPoint().getZ());
			final GaiaRegion gr = new GaiaRegion(min, max);
			if (!isUniqueRegion(world.getUID(), gr)) {
				CoreMethods.sendMessage(player, Gaia.PREFIX + ChatColor.RED + "Selected region intersects with another arena, Aborting arena creation.");
				return false;
			}
			final Arena arena = new Arena(arenaName, world, gr);
			if (!GaiaIO.getInstance().createArenaFiles(arenaName)) {
				CoreMethods.sendMessage(player, Gaia.PREFIX + ChatColor.RED + "Critical error, could not create arena file, check console for more info.");
				return false;
			}
			CoreMethods.sendMessage(player, Gaia.PREFIX + ChatColor.GREEN + "Analyzing " + arena.getFormattedName());
			final GaiaConsumerInfo info = new GaiaConsumerInfo(player,
				Gaia.PREFIX + arena.getFormattedName() + ChatColor.GREEN + " has been created!",
				Gaia.PREFIX + ChatColor.RED + "Something went wrong, couldn't create arena: " + arena.getFormattedName()
			);
			if (!CoreMethods.splitIntoChunkRegions(arena)) {
				CoreMethods.sendMessage(player, Gaia.PREFIX + info.fail);
				return false;
			}
			final long timeout = Gaia.getPlugin().getConfig().getLong("Analysis.Timeout");
			Bukkit.getScheduler().runTaskTimer(Gaia.getPlugin(), l -> {
				if (System.currentTimeMillis() > info.startTime + timeout) {
					CoreMethods.sendMessage(info.sender, info.fail);
					ArenaManager.removeArena(arena.getName());
					l.cancel();
				} else {
					if (arena.getSubRegions().stream().allMatch(GaiaChunkRegion::isAnalyzed) && arena.finalizeArena()) {
						Bukkit.getScheduler().runTaskAsynchronously(Gaia.getPlugin(), () -> {
							boolean result = GaiaIO.getInstance().saveArena(arena);
							CoreMethods.sendMessage(info.sender, result ? info.success : info.fail);
						});
						l.cancel();
					}
				}
			}, 1, 1);
			addArena(arena);
			return true;
		} catch (IncompleteRegionException e) {
			CoreMethods.sendMessage(player, Gaia.PREFIX + ChatColor.RED + "You need a WorldEdit selection in order to create an arena.");
			return false;
		}
	}
}
