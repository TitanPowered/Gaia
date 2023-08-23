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

import java.util.concurrent.TimeUnit;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.gaia.api.util.ChunkUtil;
import me.moros.gaia.benchmark.Util.PalettedSnapshot;
import me.moros.gaia.benchmark.Util.TestObject;
import me.moros.gaia.common.util.IndexedIterator;
import me.moros.math.Vector3i;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 3, time = 50, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 50, timeUnit = TimeUnit.MILLISECONDS)
public class RestoreBenchmark {
  private static final int AMOUNT = ChunkUtil.CHUNK_SIZE * ChunkUtil.CHUNK_SIZE * ChunkUtil.CHUNK_SECTION_SIZE * 24;

  @Param({"true", "false"})
  boolean aligned;

  PalettedSnapshot snapshot;

  @Setup
  public void setup() {
    var min = aligned ? Vector3i.ZERO : Vector3i.of(0, 1, 0);
    var max = Vector3i.of(15, 255, 15);
    var chunk = ChunkRegion.create(Region.of(min, max));
    var palette = Util.createPalette(64);
    snapshot = new PalettedSnapshot(chunk, palette, Util.generateData(chunk, palette));
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void iterateSequential(Blackhole bh) {
    var offset = ChunkUtil.toChunkSectionPos(snapshot.chunk().region().min());
    final int xOffset = offset.blockX();
    final int yOffset = offset.blockY();
    final int zOffset = offset.blockZ();
    final IndexedIterator<TestObject> it = snapshot.createIterator();
    int counter = 0;
    TestObject toRestore;
    while (it.hasNext() && ++counter <= AMOUNT) {
      int index = it.index();
      toRestore = it.next();
      final int y = yOffset + (index / 256);
      final int z = zOffset + ((index % 256) / 16);
      final int x = xOffset + ((index % 256) % 16);
      if (snapshot.chunk().region().contains(x, y, z)) {
        bh.consume(x);
        bh.consume(y);
        bh.consume(z);
        Blackhole.consumeCPU(64);
        bh.consume(toRestore);
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void iterate3D(Blackhole bh) {
    final int minX = snapshot.chunk().region().min().blockX();
    final int minY = snapshot.chunk().region().min().blockY();
    final int minZ = snapshot.chunk().region().min().blockZ();
    final int maxX = snapshot.chunk().region().max().blockX();
    final int maxY = snapshot.chunk().region().max().blockY();
    final int maxZ = snapshot.chunk().region().max().blockZ();
    int counter = 0;
    for (int y = minY; y <= maxY; y++) {
      for (int z = minZ; z <= maxZ; z++) {
        for (int x = minX; x <= maxX; x++) {
          if (++counter > AMOUNT) {
            return;
          }
          bh.consume(x);
          bh.consume(y);
          bh.consume(z);
          Blackhole.consumeCPU(64);
          bh.consume(snapshot.getState(x, y, z));
        }
      }
    }
  }
}
