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

package me.moros.gaia.api.arena.region;

import me.moros.gaia.api.util.ChunkUtil;
import me.moros.math.Position;
import me.moros.math.Vector3i;

/**
 * An axis-aligned immutable cuboid defined by its 2 opposing corners.
 */
public sealed interface Region permits RegionImpl {
  Vector3i min();

  Vector3i max();

  default Vector3i size() {
    return max().subtract(min()).add(1, 1, 1);
  }

  default int volume() {
    var size = size();
    return size.blockX() * size.blockY() * size.blockZ();
  }

  default Vector3i center() {
    return min().add(max()).add(Vector3i.ONE).multiply(0.5);
  }

  default boolean contains(int x, int y, int z) {
    return x >= min().blockX() && x <= max().blockX()
      && y >= min().blockY() && y <= max().blockY()
      && z >= min().blockZ() && z <= max().blockZ();
  }

  default boolean contains(Position vector) {
    double x = vector.x();
    double y = vector.y();
    double z = vector.z();
    return x >= min().x() && x <= max().x() && y >= min().y() && y <= max().y() && z >= min().z() && z <= max().z();
  }

  default boolean intersects(Region region) {
    return (min().x() <= region.max().x() && max().x() >= region.min().x()) &&
      (min().y() <= region.max().y() && max().y() >= region.min().y()) &&
      (min().z() <= region.max().z() && max().z() >= region.min().z());
  }

  static Region of(Vector3i min, Vector3i max) {
    ChunkUtil.ensureValidPosition(min);
    ChunkUtil.ensureValidPosition(max);
    return new RegionImpl(min.min(max), min.max(max));
  }
}
