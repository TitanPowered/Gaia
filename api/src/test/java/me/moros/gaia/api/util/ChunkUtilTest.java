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

package me.moros.gaia.api.util;

import java.util.Collection;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.math.Vector3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO write more tests
class ChunkUtilTest {
  @Test
  void spiralChunks() {
  }

  @Test
  void splitIntoChunks() {
    Region region = Region.of(Vector3i.ZERO, Vector3i.of(63, 63, 63));
    Collection<ChunkRegion> subRegions = ChunkUtil.splitIntoChunks(region);
    assertEquals(16, subRegions.size());
  }

  @Test
  void calculateSections() {
  }

  @Test
  void calculateChunkDistance() {
  }
}
