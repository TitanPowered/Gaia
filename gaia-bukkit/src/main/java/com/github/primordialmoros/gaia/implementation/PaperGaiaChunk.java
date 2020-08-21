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

import com.github.primordialmoros.gaia.Gaia;
import com.github.primordialmoros.gaia.api.Arena;
import com.github.primordialmoros.gaia.api.GaiaChunk;
import com.github.primordialmoros.gaia.api.GaiaData;
import com.github.primordialmoros.gaia.api.GaiaRegion;
import com.github.primordialmoros.gaia.api.GaiaVector;
import com.github.primordialmoros.gaia.io.GaiaIO;
import com.github.primordialmoros.gaia.platform.GaiaWorld;
import com.github.primordialmoros.gaia.platform.WorldWrapper;
import com.github.primordialmoros.gaia.util.functional.GaiaRunnableInfo;
import com.github.primordialmoros.gaia.util.metadata.ArenaMetadata;
import com.github.primordialmoros.gaia.util.metadata.ChunkMetadata;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.UUID;

/**
 * A chunk aligned GaiaRegion.
 */
public class PaperGaiaChunk extends GaiaChunk {

	protected PaperGaiaChunk(UUID id, Arena parent, GaiaRegion region) {
		super(id, parent, region);
	}

	@Override
	public void analyze(final GaiaRunnableInfo info, final GaiaData data) {
		PaperLib.getChunkAtAsync(((WorldWrapper) info.world).get(), getX(), getZ()).thenAccept(result -> {
			GaiaVector relative, real;
			int counter = 0;
			while (++counter <= info.maxTransactions && info.it.hasNext()) {
				relative = info.it.next();
				real = getRegion().getMinimumPoint().add(relative);
				data.setDataAt(relative, info.world.getBlockAt(real).getBlockData());
			}
			if (info.it.hasNext()) {
				Bukkit.getScheduler().runTaskLater(Gaia.getPlugin(), () -> analyze(info, data), 1);
			} else {
				Bukkit.getScheduler().runTaskAsynchronously(Gaia.getPlugin(), () -> {
					String hash = GaiaIO.getInstance().saveData(this, data);
					if (!hash.isEmpty())
						((ArenaMetadata) getParent().getMetadata()).chunks.add((ChunkMetadata) getMetadata());
				});
			}
		});
	}

	@Override
	public void revert(final GaiaRunnableInfo info, final GaiaData data) {
		if (!isReverting()) return;
		PaperLib.getChunkAtAsync(((WorldWrapper) info.world).get(), getX(), getZ()).thenAccept(result -> {
			GaiaVector relative, real;
			int counter = 0;
			while (++counter <= info.maxTransactions && info.it.hasNext()) {
				relative = info.it.next();
				real = getRegion().getMinimumPoint().add(relative);
				info.world.getBlockAt(real).setBlockData(data.getDataAt(relative));
			}
			if (info.it.hasNext()) {
				Bukkit.getScheduler().runTaskLater(Gaia.getPlugin(), () -> revert(info, data), 1);
			} else {
				cancelReverting();
			}
		});
	}

	public static void revertChunk(final GaiaChunk chunk, final GaiaWorld world) {
		if (chunk.isReverting()) return;
		chunk.startReverting();
		Bukkit.getScheduler().runTaskAsynchronously(Gaia.getPlugin(), () -> {
			final Iterator<GaiaVector> it = chunk.iterator();
			final GaiaData gd = GaiaIO.getInstance().loadData(chunk);
			if (gd != null) chunk.revert(new GaiaRunnableInfo(it, world, 4096), gd);
		});
	}

	public static void analyzeChunk(final GaiaChunk chunk, final GaiaWorld world) {
		if (chunk.isAnalyzed()) return;
		final Iterator<GaiaVector> it = chunk.iterator();
		final GaiaData gd = new GaiaData(chunk.getRegion().getVector());
		chunk.analyze(new GaiaRunnableInfo(it, world, 4096), gd);
	}
}
