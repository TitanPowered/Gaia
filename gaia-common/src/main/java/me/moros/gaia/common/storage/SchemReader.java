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

import java.io.Closeable;
import java.io.IOException;

import me.moros.gaia.api.chunk.ChunkData;
import me.moros.gaia.api.region.ChunkRegion;
import me.moros.gaia.api.region.Region;
import me.moros.math.Vector3i;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTagType;

public class SchemReader implements Closeable {
  private final LinStream rootStream;
  private final Decoder decoder;

  public SchemReader(LinStream rootStream, Decoder decoder) {
    this.rootStream = rootStream;
    this.decoder = decoder;
  }

  public ChunkData read() throws IOException {
    LinCompoundTag schematicTag = getBaseTag();
    int schematicVersion = schematicTag.getTag("Version", LinTagType.intTag()).valueAsInt();
    if (schematicVersion != 3) {
      throw new IllegalStateException("Unknown schematic version " + schematicVersion);
    }
    return readVersion3(schematicTag);
  }

  private LinCompoundTag getBaseTag() throws IOException {
    return LinRootEntry.readFrom(rootStream).value().getTag("Schematic", LinTagType.compoundTag());
  }

  private ChunkData readVersion3(LinCompoundTag schematicTag) throws IOException {
    int width = schematicTag.getTag("Width", LinTagType.shortTag()).valueAsShort() & 0xFFFF;
    int height = schematicTag.getTag("Height", LinTagType.shortTag()).valueAsShort() & 0xFFFF;
    int length = schematicTag.getTag("Length", LinTagType.shortTag()).valueAsShort() & 0xFFFF;

    Vector3i offset = decoder.decodeVector3i(schematicTag.findTag("Offset", LinTagType.intArrayTag()));
    ChunkRegion chunk = ChunkRegion.create(Region.of(offset, offset.add(width, height, length).subtract(Vector3i.ONE)));

    LinCompoundTag blockContainer = schematicTag.getTag("Blocks", LinTagType.compoundTag());
    LinCompoundTag paletteObject = blockContainer.getTag("Palette", LinTagType.compoundTag());
    byte[] blocks = blockContainer.getTag("Data", LinTagType.byteArrayTag()).value();
    return decoder.decodeBlocks(chunk, paletteObject, blocks);
  }

  @Override
  public void close() throws IOException {
  }
}
