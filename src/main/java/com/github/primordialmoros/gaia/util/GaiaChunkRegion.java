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

package com.github.primordialmoros.gaia.util;

import com.github.primordialmoros.gaia.Gaia;
import com.github.primordialmoros.gaia.util.functional.GaiaRunnableInfo;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A chunk aligned GaiaRegion that stores BlockData.
 */
public class GaiaChunkRegion {

	public static final Comparator<GaiaChunkRegion> YZX_ORDER = Comparator.comparingInt(GaiaChunkRegion::getZ).thenComparingInt(GaiaChunkRegion::getX);

	private GaiaData data;
	private final GaiaRegion chunk;
	private final int chunkX, chunkZ;

	private boolean reverting = false;

	public GaiaChunkRegion(final GaiaRegion region) {
		this(region, null);
	}

	public GaiaChunkRegion(final GaiaRegion region, final GaiaData data) {
		chunkX = region.getMinimumPoint().getX() / 16;
		chunkZ = region.getMinimumPoint().getZ() / 16;
		chunk = region;
		this.data = data;
	}

	public int getX() {
		return chunkX;
	}

	public int getZ() {
		return chunkZ;
	}

	public GaiaRegion getRegion() {
		return chunk;
	}

	public GaiaData getGaiaData() {
		return data;
	}

	/**
	 * Attempts to load the chunk and analyze blocks based on passed info.
	 * It will analyze up to a maximum amount of blocks per tick as defined in passed info.
	 * If there are more blocks left to analyze it will continue in the next tick.
	 *
	 * @param info the object containing the info
	 */
	private void analyze(final GaiaRunnableInfo info, final BlockData[][][] bd) {
		PaperLib.getChunkAtAsync(info.world, chunkX, chunkZ).thenAccept(result -> {
			GaiaVector relative, real;
			BlockData d;
			int counter = 0;
			while (++counter <= info.maxTransactions && info.it.hasNext()) {
				relative = info.it.next();
				real = chunk.getMinimumPoint().add(relative);
				d = info.world.getBlockAt(real.getX(), real.getY(), real.getZ()).getBlockData();
				bd[relative.getX()][relative.getY()][relative.getZ()] = d;
			}
			if (info.it.hasNext()) {
				Bukkit.getScheduler().runTaskLater(Gaia.getPlugin(), () -> analyze(info, bd), 1);
			} else {
				data = new GaiaData(bd);
			}
		});
	}

	/**
	 * Attempts to load the chunk and revert blocks based on passed info.
	 * It will revert up to a maximum amount of blocks per tick as defined in passed info.
	 * If there are more blocks left to revert it will continue in the next tick.
	 *
	 * @param info the object containing the info
	 */
	private void revert(final GaiaRunnableInfo info) {
		if (!reverting) return;
		PaperLib.getChunkAtAsync(info.world, chunkX, chunkZ).thenAccept(result -> {
			GaiaVector relative, real;
			int counter = 0;
			while (++counter <= info.maxTransactions && info.it.hasNext()) {
				relative = info.it.next();
				real = chunk.getMinimumPoint().add(relative);
				info.world.getBlockAt(real.getX(), real.getY(), real.getZ()).setBlockData(getGaiaData().getDataAt(relative));
			}
			if (info.it.hasNext()) {
				Bukkit.getScheduler().runTaskLater(Gaia.getPlugin(), () -> revert(info), 1);
			} else {
				reverting = false;
			}
		});
	}

	public boolean isReverting() {
		return reverting;
	}

	public void cancelReverting() {
		reverting = false;
	}

	public boolean isAnalyzed() {
		return data != null;
	}

	public static void analyzeChunk(final GaiaChunkRegion chunk, final World world) {
		if (chunk.isAnalyzed()) return;
		final Iterator<GaiaVector> it = chunk.iterator();
		final GaiaVector v = chunk.getRegion().getVector();
		final BlockData[][][] bd = new BlockData[v.getX()][v.getY()][v.getZ()];
		chunk.analyze(new GaiaRunnableInfo(it, world, 4096), bd);
	}

	public static void revertChunk(final GaiaChunkRegion chunk, final World world) {
		if (!chunk.isAnalyzed() || chunk.isReverting()) return;
		chunk.reverting = true;
		final Iterator<GaiaVector> it = chunk.iterator();
		chunk.revert(new GaiaRunnableInfo(it, world, 4096));
	}

	public Iterator<GaiaVector> iterator() {
		return new Iterator<GaiaVector>() {
			private final GaiaVector max = chunk.getVector();
			private int nextX = 0;
			private int nextY = 0;
			private int nextZ = 0;

			@Override
			public boolean hasNext() {
				return (nextX != Integer.MIN_VALUE);
			}

			@Override
			public GaiaVector next() {
				if (!hasNext()) throw new NoSuchElementException();
				GaiaVector answer = GaiaVector.at(nextX, nextY, nextZ);
				if (++nextX >= max.getX()) {
					nextX = 0;
					if (++nextZ >= max.getZ()) {
						nextZ = 0;
						if (++nextY >= max.getY()) {
							nextX = Integer.MIN_VALUE;
						}
					}
				}
				return answer;
			}
		};
	}
}
