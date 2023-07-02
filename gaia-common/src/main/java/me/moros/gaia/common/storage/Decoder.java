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

package me.moros.gaia.common.storage;

import java.io.IOException;

import me.moros.gaia.api.chunk.ChunkData;
import me.moros.gaia.api.region.ChunkRegion;
import me.moros.math.Vector3i;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntArrayTag;

public interface Decoder {
  default Vector3i decodeVector3i(@Nullable LinIntArrayTag tag) throws IOException {
    if (tag == null) {
      return Vector3i.ZERO;
    }
    try {
      return Vector3i.from(tag.value());
    } catch (IllegalArgumentException e) {
      throw new IOException(e);
    }
  }

  int dataVersion();

  ChunkData decodeBlocks(ChunkRegion chunk, LinCompoundTag paletteObject, byte[] blocks) throws IOException;
}