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

package me.moros.gaia.api.service;

import java.util.Optional;
import java.util.stream.Stream;

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.RevertResult;
import me.moros.gaia.api.arena.region.Region;
import me.moros.math.Position;
import net.kyori.adventure.key.Key;

public interface ArenaService extends Iterable<Arena> {
  boolean contains(String name);

  Optional<Arena> arena(String name);

  default Optional<Arena> arena(Key level, Region region) {
    return stream().filter(a -> a.level().equals(level) && a.region().intersects(region)).findAny();
  }

  default Optional<Arena> arena(Key level, Position position) {
    return stream().filter(a -> a.level().equals(level) && a.region().contains(position)).findAny();
  }

  boolean add(Arena arena);

  boolean remove(String name);

  int size();

  Stream<Arena> stream();

  RevertResult revert(Arena arena);
}
