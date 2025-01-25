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

package me.moros.gaia.api.arena;

import me.moros.math.Position;

record PointImpl(Position position, float yaw, float pitch) implements Point {
  @Override
  public double x() {
    return position().x();
  }

  @Override
  public double y() {
    return position().y();
  }

  @Override
  public double z() {
    return position().z();
  }

  @Override
  public int blockX() {
    return position().blockX();
  }

  @Override
  public int blockY() {
    return position().blockY();
  }

  @Override
  public int blockZ() {
    return position().blockZ();
  }

  @Override
  public String toString() {
    return position().toString();
  }
}
