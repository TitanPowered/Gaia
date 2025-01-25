/*
 * Copyright 2020-2025 Moros
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.math.Position;
import me.moros.math.Vector3i;

public final class ChunkUtil {
  private ChunkUtil() {
  }

  public static final int CHUNK_SIZE = 16;
  public static final int CHUNK_SECTION_SIZE = 16;
  public static final int CHUNK_SECTION_VOLUME = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SECTION_SIZE;

  private static final int WORLD_XZ_MINMAX = 30_000_000;
  private static final int WORLD_Y_MIN = -2048;
  private static final int WORLD_Y_MAX = 2047;
  private static final int BIT_MASK = 0xFFFFFFF0;

  private static boolean isValidXZ(int x, int z) {
    return -WORLD_XZ_MINMAX <= x && x <= WORLD_XZ_MINMAX && -WORLD_XZ_MINMAX <= z && z <= WORLD_XZ_MINMAX;
  }

  private static boolean isValidY(int n) {
    return WORLD_Y_MIN <= n && n <= WORLD_Y_MAX;
  }

  public static boolean isValidPosition(Position pos) {
    return isValidXZ(pos.blockX(), pos.blockZ()) && isValidY(pos.blockY());
  }

  public static void ensureValidPosition(Position pos) throws IllegalArgumentException {
    if (!isValidPosition(pos)) {
      throw new IllegalArgumentException(String.format("Position %s exceeds bounds!", pos));
    }
  }

  public static int toChunkPos(int value) {
    return value >> 4;
  }

  public static Vector3i toChunkSectionPos(Position position) {
    return Vector3i.of(position.blockX() & BIT_MASK, position.blockY() & BIT_MASK, position.blockZ() & BIT_MASK);
  }

  public static int calculateChunkVolume(ChunkRegion chunk) {
    int sections = calculateSections(chunk.region());
    return CHUNK_SIZE * CHUNK_SIZE * CHUNK_SECTION_SIZE * sections;
  }

  public static List<ChunkPosition> spiralChunks(Region region) {
    final int sizeX = calculateChunkDistance(region.min().blockX(), region.max().blockX());
    final int sizeZ = calculateChunkDistance(region.min().blockZ(), region.max().blockZ());

    final var centerChunk = ChunkPosition.at(region.center());

    final int halfX = sizeX / 2;
    final int halfZ = sizeZ / 2;

    int x = 0, z = 0, dx = 0, dz = -1;
    int t = Math.max(sizeX, sizeZ);
    final int maxI = t * t;
    final List<ChunkPosition> result = new ArrayList<>();
    for (int i = 0; i < maxI; i++) {
      if ((-halfX <= x) && (x <= halfX) && (-halfZ <= z) && (z <= halfZ)) {
        result.add(ChunkPosition.at(centerChunk.x() + x, centerChunk.z() + z));
      }
      if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
        t = dx;
        dx = -dz;
        dz = t;
      }
      x += dx;
      z += dz;
    }
    return result;
  }

  public static Collection<ChunkRegion> splitIntoChunks(Region region) {
    final int minX = region.min().blockX();
    final int maxX = region.max().blockX();

    final int minY = region.min().blockY();
    final int maxY = region.max().blockY();

    final int minZ = region.min().blockZ();
    final int maxZ = region.max().blockZ();

    final int dx = calculateChunkDistance(minX, maxX);
    final int dz = calculateChunkDistance(minZ, maxZ);

    final Collection<ChunkRegion> regions = new ArrayList<>(dx * dz);
    int tempX, tempZ;
    Vector3i v1, v2;
    for (int x = toChunkPos(minX); x <= toChunkPos(maxX); ++x) {
      tempX = x * CHUNK_SIZE;
      for (int z = toChunkPos(minZ); z <= toChunkPos(maxZ); ++z) {
        tempZ = z * CHUNK_SIZE;
        v1 = atXZClamped(tempX, minY, tempZ, minX, maxX, minZ, maxZ);
        v2 = atXZClamped(tempX + (CHUNK_SIZE - 1), maxY, tempZ + (CHUNK_SIZE - 1), minX, maxX, minZ, maxZ);
        regions.add(ChunkRegion.create(Region.of(v1, v2)));
      }
    }
    return regions;
  }

  public static boolean isValidRegionSize(Region region) {
    return region.size().blockX() <= CHUNK_SIZE && region.size().blockZ() <= CHUNK_SIZE;
  }

  public static void validateRegionSize(Region region) throws IllegalArgumentException {
    if (!isValidRegionSize(region)) {
      throw new IllegalArgumentException(region.size() + " exceeds chunk size limits!");
    }
  }

  public static int calculateSections(Region region) {
    return 1 + (toChunkPos(region.max().blockY()) - toChunkPos(region.min().blockY()));
  }

  public static int calculateChunkDistance(int minPos, int maxPos) {
    if (minPos > maxPos) {
      throw new IllegalArgumentException(String.format("Encountered minPos (%d) > maxPos (%d)", minPos, maxPos));
    }
    return Math.max(1, toChunkPos(maxPos) - toChunkPos(minPos));
  }

  private static Vector3i atXZClamped(int x, int y, int z, int minX, int maxX, int minZ, int maxZ) {
    if (minX > maxX || minZ > maxZ) {
      throw new IllegalArgumentException("Minimum cannot be greater than maximum");
    }
    return Vector3i.of(Math.max(minX, Math.min(maxX, x)), y, Math.max(minZ, Math.min(maxZ, z)));
  }
}
