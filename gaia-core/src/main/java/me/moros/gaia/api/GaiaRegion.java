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

package me.moros.gaia.api;

import com.sk89q.worldedit.math.BlockVector3;

/**
 * An axis-aligned immutable cuboid defined by it's 2 opposing corners.
 */
public record GaiaRegion(BlockVector3 min, BlockVector3 max) {
  public GaiaRegion(BlockVector3 min, BlockVector3 max) {
    BlockVector3.checkLongPackable(min);
    BlockVector3.checkLongPackable(max);
    this.min = min.getMinimum(max);
    this.max = max.getMaximum(max);
  }

  public BlockVector3 size() {
    return max().subtract(min()).add(1, 1, 1);
  }

  public int volume() {
    BlockVector3 size = size();
    return size.getX() * size.getY() * size.getZ();
  }

  public BlockVector3 center() {
    return min().add(max()).divide(2);
  }

  public boolean contains(BlockVector3 vector) {
    return vector.containedWithin(min(), max());
  }

  public boolean intersects(GaiaRegion region) {
    return (min().getX() <= region.max().getX() && max().getX() >= region.min().getX()) &&
      (min().getY() <= region.max().getY() && max().getY() >= region.min().getY()) &&
      (min().getZ() <= region.max().getZ() && max().getZ() >= region.min().getZ());
  }
}
