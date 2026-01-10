/*
 * Copyright 2020-2026 Moros
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

package me.moros.gaia.common.platform.codec;

import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;

record PaletteDecoderImpl<R>(SimpleCodec<R> codec) implements PaletteDecoder<R> {
  @Override
  public Int2ObjectMap<R> apply(LinCompoundTag paletteObject, int srcVersion) throws IOException {
    var entrySet = paletteObject.value().entrySet();
    Int2ObjectMap<R> palette = new Int2ObjectArrayMap<>(entrySet.size());
    for (var palettePart : entrySet) {
      if (palettePart.getValue() instanceof LinIntTag idTag) {
        String raw = palettePart.getKey();
        palette.put(idTag.valueAsInt(), codec().fromString(raw, srcVersion));
      } else {
        throw new IOException("Invalid palette entry: " + palettePart);
      }
    }
    return palette;
  }
}
