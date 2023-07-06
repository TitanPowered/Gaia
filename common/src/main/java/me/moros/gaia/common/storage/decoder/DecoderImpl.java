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

package me.moros.gaia.common.storage.decoder;

import java.io.IOException;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.common.platform.GaiaSnapshot;
import net.minecraft.SharedConstants;
import net.minecraft.world.level.block.state.BlockState;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;

final class DecoderImpl implements Decoder {
  private final int dataVersion;
  private final Function<String, BlockState> mapper;

  DecoderImpl(Function<String, BlockState> mapper) {
    this.dataVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    this.mapper = mapper;
  }

  @Override
  public int dataVersion() {
    return dataVersion;
  }

  @Override
  public Snapshot decodeBlocks(ChunkRegion chunk, LinCompoundTag paletteObject, byte[] blocks) throws IOException {
    var palette = decodePalette(paletteObject);
    return new GaiaSnapshot(chunk, palette, blocks);
  }

  private Int2ObjectMap<BlockState> decodePalette(LinCompoundTag paletteObject) throws IOException {
    var entrySet = paletteObject.value().entrySet();
    Int2ObjectMap<BlockState> palette = new Int2ObjectArrayMap<>(entrySet.size());
    for (var palettePart : entrySet) {
      if (palettePart.getValue() instanceof LinIntTag idTag) {
        palette.put(idTag.valueAsInt(), mapper.apply(palettePart.getKey()));
      } else {
        throw new IOException("Invalid palette entry: " + palettePart);
      }
    }
    return palette;
  }
}
