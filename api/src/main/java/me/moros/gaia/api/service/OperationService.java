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

package me.moros.gaia.api.service;

import java.util.concurrent.CompletableFuture;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.operation.GaiaOperation;
import me.moros.gaia.api.platform.Level;

public interface OperationService {
  void shutdown();

  int remainingOperations();

  <T> CompletableFuture<T> add(GaiaOperation.ChunkOperation<T> operation);

  void cancel(Level level, ChunkRegion chunkRegion);
}