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

import java.util.List;
import java.util.stream.Stream;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public sealed interface Arena extends Reversible, PointHolder.Mutable, Iterable<ChunkRegion.Validated> permits ArenaImpl {
  String name();

  default Component displayName() {
    return Component.text(name(), NamedTextColor.GOLD);
  }

  Key level();

  Region region();

  Component info();

  List<ChunkRegion.Validated> chunks();

  Stream<ChunkRegion.Validated> streamChunks();

  default boolean reverting() {
    return streamChunks().anyMatch(Reversible::reverting);
  }

  static Builder builder() {
    return new Builder();
  }
}
