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

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import me.moros.gaia.api.chunk.ChunkData;
import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.api.region.ChunkRegion;
import net.kyori.adventure.key.Keyed;

public interface Level extends Keyed {
  boolean restoreSnapshot(ChunkData data, int amount);

  CompletableFuture<ChunkData> snapshot(ChunkRegion chunk);

  default CompletableFuture<?> loadChunkWithTicket(ChunkPosition pos) {
    return loadChunkWithTicket(pos.x(), pos.z());
  }

  CompletableFuture<?> loadChunkWithTicket(int x, int z);

  default void removeChunkTicket(ChunkPosition pos) {
    removeChunkTicket(pos.x(), pos.z());
  }

  void removeChunkTicket(int x, int z);

  void fixLight(Collection<ChunkPosition> positions);
}
