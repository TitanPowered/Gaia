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

package me.moros.gaia;

import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;

import me.moros.gaia.api.GaiaChunkPos;

@FunctionalInterface
public interface LightFixer extends BiConsumer<UUID, Collection<GaiaChunkPos>> {
  enum Mode {DISABLED, POST_CHUNK, POST_ARENA}

  default void onChunkRelight(int x, int z) {
  }

  default void onComplete(int affected) {
  }
}
