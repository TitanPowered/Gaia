/*
 * Copyright 2020-2022 Moros
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

package me.moros.gaia.api;

import com.sk89q.worldedit.math.BlockVector3;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An axis-aligned immutable cuboid. It can be defined using a world and two corners of the cuboid.
 */
public final class GaiaRegion {
  private final BlockVector3 minPoint, maxPoint, diff;

  public GaiaRegion(@NonNull BlockVector3 pos1, @NonNull BlockVector3 pos2) {
    BlockVector3.checkLongPackable(pos1);
    BlockVector3.checkLongPackable(pos2);
    minPoint = pos1.getMinimum(pos2);
    maxPoint = pos2.getMaximum(pos2);
    diff = maxPoint.subtract(minPoint).add(1, 1, 1);
  }

  public @NonNull BlockVector3 min() {
    return minPoint;
  }

  public @NonNull BlockVector3 max() {
    return maxPoint;
  }

  public @NonNull BlockVector3 size() {
    return diff;
  }

  public int volume() {
    return diff.getX() * diff.getY() * diff.getZ();
  }

  public @NonNull BlockVector3 center() {
    return min().add(max()).divide(2);
  }

  public boolean contains(@NonNull BlockVector3 vector) {
    return vector.containedWithin(min(), max());
  }

  public boolean intersects(@NonNull GaiaRegion region) {
    return (min().getX() <= region.max().getX() && max().getX() >= region.min().getX()) &&
      (min().getY() <= region.max().getY() && max().getY() >= region.min().getY()) &&
      (min().getZ() <= region.max().getZ() && max().getZ() >= region.min().getZ());
  }
}
