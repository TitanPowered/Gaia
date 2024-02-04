/*
 * Copyright 2020-2024 Moros
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

package me.moros.gaia.common.storage.decoder;

import java.io.IOException;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.Snapshot;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.slf4j.Logger;

public interface Decoder {
  int dataVersion();

  Snapshot decodeBlocks(ChunkRegion chunk, LinCompoundTag paletteObject, byte[] blocks, int srcVersion) throws IOException;

  static Decoder createVanilla(Logger logger) {
    return new DecoderImpl(logger);
  }
}
