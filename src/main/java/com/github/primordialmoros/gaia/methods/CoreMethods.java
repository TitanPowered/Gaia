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

package com.github.primordialmoros.gaia.methods;

import com.github.primordialmoros.gaia.Arena;
import com.github.primordialmoros.gaia.Gaia;
import com.github.primordialmoros.gaia.util.GaiaChunkRegion;
import com.github.primordialmoros.gaia.util.GaiaRegion;
import com.github.primordialmoros.gaia.util.GaiaVector;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CoreMethods {

	private static final BlockData AIR = Material.AIR.createBlockData();

	public static void sendMessage(final CommandSender sender, final BaseComponent[] text) {
		sendMessage(sender, new TextComponent(text));
	}

	public static void sendMessage(final CommandSender sender, final String text) {
		sendMessage(sender, new TextComponent(text));
	}

	public static void sendMessage(final CommandSender sender, final TextComponent text) {
		if (sender == null) return;
		if (sender instanceof Player && !((Player) sender).isOnline()) return;
		sender.spigot().sendMessage(text);
	}

	public static boolean splitIntoChunkRegions(final Arena arena) {
    	final int minX = arena.getRegion().getMinimumPoint().getX();
    	final int maxX = arena.getRegion().getMaximumPoint().getX();
		final int minY = arena.getRegion().getMinimumPoint().getY();
		final int maxY = arena.getRegion().getMaximumPoint().getY();
		final int minZ = arena.getRegion().getMinimumPoint().getZ();
		final int maxZ = arena.getRegion().getMaximumPoint().getZ();

		int tempX, tempZ;
		GaiaVector v1, v2;
		for (int x = minX >> 4; x <= maxX >> 4; ++x) {
			tempX = x*16;
    		for (int z = minZ >> 4; z <= maxZ >> 4; ++z) {
				tempZ = z*16;
				v1 = GaiaVector.atXZClamped(tempX, minY, tempZ, minX, maxX, minZ, maxZ);
    			v2 = GaiaVector.atXZClamped(tempX + 15, maxY, tempZ + 15, minX, maxX, minZ, maxZ);
				final GaiaChunkRegion chunkRegion = new GaiaChunkRegion(new GaiaRegion(v1, v2));
				arena.addSubRegion(chunkRegion);
				GaiaChunkRegion.analyzeChunk(chunkRegion, arena.getWorld());
    		}
    	}
		return arena.getSubRegions().size() > 0;
	}

	public static BlockData getBlockDataFromString(final String value) {
		try {
			return Bukkit.createBlockData(value);
		} catch (IllegalArgumentException e) {
			Gaia.getLog().warning("Invalid block data in palette: " + value + ". Block will be replaced with air.");
			return AIR;
		}
	}

	public static World getWorld(final UUID uid) {
		final World world = Bukkit.getWorld(uid);
		if (world == null) throw new IllegalArgumentException("Couldn't find world with UID " + uid);
		return world;
	}
}
