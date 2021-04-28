/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *   Copyright (C) sk89q <http://www.sk89q.com>
 *   Copyright (C) WorldEdit team and contributors
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

package me.moros.gaia.io;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import me.moros.gaia.api.GaiaData;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GaiaWriter implements Closeable {
  private final NBTOutputStream outputStream;

  /**
   * Create a new schematic writer.
   * @param outputStream the output stream to write to
   */
  protected GaiaWriter(@NonNull NBTOutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public void write(@NonNull GaiaData data) throws IOException {
    int width = data.getVector().getX();
    int height = data.getVector().getY();
    int length = data.getVector().getZ();

    Map<String, Tag> schematic = new HashMap<>();
    schematic.put("Width", new ShortTag((short) width));
    schematic.put("Height", new ShortTag((short) height));
    schematic.put("Length", new ShortTag((short) length));

    int paletteMax = 0;
    Map<String, Integer> palette = new HashMap<>();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);
    for (int y = 0; y < height; y++) {
      for (int z = 0; z < length; z++) {
        for (int x = 0; x < width; x++) {
          String blockKey = data.getDataAt(x, y, z).getAsString();
          int blockId;
          if (palette.containsKey(blockKey)) {
            blockId = palette.get(blockKey);
          } else {
            blockId = paletteMax;
            palette.put(blockKey, blockId);
            paletteMax++;
          }
          while ((blockId & -128) != 0) {
            buffer.write(blockId & 127 | 128);
            blockId >>>= 7;
          }
          buffer.write(blockId);
        }
      }
    }
    schematic.put("PaletteMax", new IntTag(paletteMax));
    Map<String, Tag> paletteTag = new HashMap<>();
    palette.forEach((key, value) -> paletteTag.put(key, new IntTag(value)));
    schematic.put("Palette", new CompoundTag(paletteTag));
    schematic.put("BlockData", new ByteArrayTag(buffer.toByteArray()));
    outputStream.writeNamedTag("Schematic", new CompoundTag(schematic));
  }

  @Override
  public void close() throws IOException {
    outputStream.close();
  }
}
