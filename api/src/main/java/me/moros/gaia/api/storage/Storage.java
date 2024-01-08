/*
 * Copyright 2020-2024 Moros
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

package me.moros.gaia.api.storage;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.Snapshot;

public interface Storage {
  boolean arenaFileExists(String name);

  boolean createEmptyArenaFiles(String name);

  boolean deleteArena(String name);

  CompletableFuture<Iterable<Arena>> loadAllArenas();

  CompletableFuture<Arena> saveArena(Arena arena);

  CompletableFuture<Collection<Snapshot>> loadDataAsync(String name, Collection<ChunkRegion.Validated> chunkRegions);

  CompletableFuture<Collection<ChunkRegion.Validated>> saveDataAsync(String name, Iterable<Snapshot> snapshots);
}
