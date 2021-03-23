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

package me.moros.gaia.api;

import me.moros.gaia.util.functional.GaiaRunnableInfo;
import me.moros.gaia.util.metadata.ChunkMetadata;
import me.moros.gaia.util.metadata.GaiaMetadata;
import me.moros.gaia.util.metadata.Metadatable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * A chunk aligned GaiaRegion.
 */
public abstract class GaiaChunk implements Metadatable {
	public static final Comparator<GaiaChunk> ZX_ORDER = Comparator.comparingInt(GaiaChunk::getZ).thenComparingInt(GaiaChunk::getX);

	private final UUID id;

	private final Arena parent;
	private final GaiaRegion chunk;
	private final int chunkX, chunkZ;

	private ChunkMetadata meta;

	private boolean reverting;

	protected GaiaChunk(@NonNull UUID id, @NonNull Arena parent, @NonNull GaiaRegion region) {
		this.id = id;
		this.parent = parent;
		chunkX = region.getMinimumPoint().getX() / 16;
		chunkZ = region.getMinimumPoint().getZ() / 16;
		chunk = region;
		reverting = false;
		parent.addSubRegion(this);
	}

	public @NonNull UUID getId() {
		return id;
	}

	public @NonNull Arena getParent() {
		return parent;
	}

	public int getX() {
		return chunkX;
	}

	public int getZ() {
		return chunkZ;
	}

	public @NonNull GaiaRegion getRegion() {
		return chunk;
	}

	public boolean isReverting() {
		return reverting;
	}

	public void startReverting() {
		reverting = true;
	}

	public void cancelReverting() {
		reverting = false;
	}

	public boolean isAnalyzed() {
		return meta != null && meta.hash != null && !meta.hash.isEmpty();
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

	@Override
	public GaiaMetadata getMetadata() {
		return meta;
	}

	@Override
	public void setMetadata(GaiaMetadata meta) {
		this.meta = (ChunkMetadata) meta;
	}

	/**
	 * Attempts to load the chunk and analyze blocks based on passed info.
	 * It will analyze up to a maximum amount of blocks per tick as defined in passed info.
	 * If there are more blocks left to analyze it will continue in the next tick.
	 * @param info the object containing the info
	 * @param data the object containing the data
	 */
	public abstract void analyze(@NonNull GaiaRunnableInfo info, @NonNull GaiaData data);

	/**
	 * Attempts to load the chunk and revert blocks based on passed info.
	 * It will revert up to a maximum amount of blocks per tick as defined in passed info.
	 * If there are more blocks left to revert it will continue in the next tick.
	 * @param info the object containing the info
	 * @param data the object containing the data
	 */
	public abstract void revert(@NonNull GaiaRunnableInfo info, @NonNull GaiaData data);
}
