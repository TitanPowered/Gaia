/*
 * Copyright 2020-2023 Moros
 *
 * This file is part of Gaia.
 *
 * Gaia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gaia is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gaia. If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.gaia.common.platform;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.common.util.IndexedIterator;
import me.moros.gaia.common.util.VarIntIterator;
import net.minecraft.world.level.block.state.BlockState;

public final class GaiaSnapshot implements Snapshot {
  private final ChunkRegion chunk;
  private final Int2ObjectMap<BlockState> palette;
  private final byte[] data;
  private VarIntIterator cachedIterator;

  public GaiaSnapshot(ChunkRegion chunk, Int2ObjectMap<BlockState> palette, byte[] data) {
    this.chunk = chunk;
    this.palette = palette;
    this.data = data;
    resetIterator();
  }

  @Override
  public ChunkRegion chunk() {
    return chunk;
  }

  @Override
  public String getStateString(int x, int y, int z) {
    throw new UnsupportedOperationException();
  }

  public void resetIterator() {
    this.cachedIterator = new VarIntIterator(this.data);
  }

  public IndexedIterator<BlockState> iterator() {
    return new IndexedIterator<>(cachedIterator, palette::get);
  }
}
