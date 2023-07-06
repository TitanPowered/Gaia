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

package me.moros.gaia.api.chunk;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.util.ChunkUtil;

public interface Snapshot extends ChunkPosition {
  ChunkRegion chunk();

  default int x() {
    return chunk().x();
  }

  default int z() {
    return chunk().z();
  }

  String getStateString(int x, int y, int z);

  default int sections() {
    return ChunkUtil.calculateSections(chunk().region());
  }

  default int width() {
    return ChunkUtil.CHUNK_SIZE;
  }

  default int height() {
    return sections() * ChunkUtil.CHUNK_SECTION_SIZE;
  }

  default int length() {
    return ChunkUtil.CHUNK_SIZE;
  }
}
