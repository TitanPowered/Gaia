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

package me.moros.gaia.api.platform;

import java.util.Optional;

import me.moros.gaia.api.arena.Point;
import me.moros.math.Vector3d;
import net.kyori.adventure.key.Key;

public interface Locatable {
  Optional<Key> level();

  default Vector3d position() {
    return Vector3d.ZERO;
  }

  default float yaw() {
    return 0;
  }

  default float pitch() {
    return 0;
  }

  default Optional<Point> createPoint() {
    return level().map(ignore -> Point.of(position(), yaw(), pitch()));
  }
}
