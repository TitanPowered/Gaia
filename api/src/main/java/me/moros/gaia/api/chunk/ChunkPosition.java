/*
 * Copyright 2020-2025 Moros
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

package me.moros.gaia.api.chunk;

import java.util.Comparator;

import me.moros.gaia.api.util.ChunkUtil;
import me.moros.math.Position;

public interface ChunkPosition {
  Comparator<ChunkPosition> ZX_ORDER = Comparator.comparingInt(ChunkPosition::x).thenComparingInt(ChunkPosition::z);

  int x();

  int z();

  static ChunkPosition at(Position position) {
    return at(ChunkUtil.toChunkPos(position.blockX()), ChunkUtil.toChunkPos(position.blockZ()));
  }

  static ChunkPosition at(int x, int z) {
    return new ChunkPositionImpl(x, z);
  }
}
