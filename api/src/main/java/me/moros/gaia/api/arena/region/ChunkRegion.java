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

package me.moros.gaia.api.arena.region;

import me.moros.gaia.api.arena.Reversible;
import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.api.util.ChunkUtil;

public sealed interface ChunkRegion extends ChunkPosition permits ChunkRegion.Validated, ChunkRegionImpl {
  Region region();

  sealed interface Validated extends ChunkRegion, Reversible.Mutable permits ChunkRegionImpl.Validated {
    long checksum();
  }

  static ChunkRegion create(Region region) {
    ChunkUtil.validateRegionSize(region);
    var chunkPos = ChunkPosition.at(region.min());
    return new ChunkRegionImpl(chunkPos.x(), chunkPos.z(), region);
  }

  static ChunkRegion.Validated create(Region region, long checksum) {
    return new ChunkRegionImpl.Validated(create(region), checksum);
  }
}
