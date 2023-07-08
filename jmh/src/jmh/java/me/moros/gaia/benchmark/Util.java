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

package me.moros.gaia.benchmark;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ThreadLocalRandom;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.api.util.ChunkUtil;
import me.moros.gaia.common.util.IndexedIterator;
import me.moros.gaia.common.util.VarIntIterator;

public class Util {
  private static final int ENTROPY = 600_600;

  public static Int2ObjectMap<TestObject> createPalette(int paletteSize) {
    Int2ObjectMap<TestObject> palette = new Int2ObjectArrayMap<>(paletteSize);
    for (int i = 0; i < paletteSize; i++) {
      palette.put(i, new TestObject(ThreadLocalRandom.current().nextInt(ENTROPY)));
    }
    return palette;
  }

  public static byte[] generateData(ChunkRegion chunk, Int2ObjectMap<TestObject> palette) {
    var snapshot = new EmptySnapshot(chunk);
    int width = snapshot.width();
    int height = snapshot.height();
    int length = snapshot.length();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);
    var rand = ThreadLocalRandom.current();
    for (int y = 0; y < height; y++) {
      for (int z = 0; z < length; z++) {
        for (int x = 0; x < width; x++) {
          int id = rand.nextInt(palette.size());
          while ((id & -128) != 0) {
            buffer.write(id & 127 | 128);
            id >>>= 7;
          }
          buffer.write(id);
        }
      }
    }
    return buffer.toByteArray();
  }

  public record TestObject(int value) {
  }

  private static final TestObject DEFAULT = new TestObject(0);

  record PalettedSnapshot(ChunkRegion chunk, Int2ObjectMap<TestObject> palette, byte[] data) implements Snapshot {
    @Override
    public String getStateString(int x, int y, int z) {
      return "";
    }

    IndexedIterator<TestObject> createIterator() {
      return new IndexedIterator<>(new VarIntIterator(data, ChunkUtil.calculateChunkVolume(chunk)), palette::get);
    }

    TestObject getState(int x, int y, int z) {
      return palette.getOrDefault(y + x * z, DEFAULT);
    }
  }

  private record EmptySnapshot(ChunkRegion chunk) implements Snapshot {
    @Override
    public String getStateString(int x, int y, int z) {
      return "";
    }
  }
}
