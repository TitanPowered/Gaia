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

package me.moros.gaia.api.operation;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.api.platform.Level;

public interface GaiaOperation<T> {
  long startTime();

  Result update();

  CompletableFuture<T> asFuture();

  enum Result {CONTINUE, WAIT, REMOVE}

  interface ChunkOperation<T> extends GaiaOperation<T>, ChunkPosition {
    ChunkRegion chunk();

    Level level();
  }

  sealed interface Revert extends ChunkOperation<Void> permits RevertOp {
  }

  sealed interface Analyze extends ChunkOperation<Snapshot> permits AnalyzeOp {
  }

  static Revert revert(Level level, Snapshot snapshot) {
    Objects.requireNonNull(level);
    Objects.requireNonNull(snapshot);
    return new RevertOp(level, snapshot);
  }

  static Analyze snapshotAnalyze(Level level, ChunkRegion chunk) {
    Objects.requireNonNull(level);
    Objects.requireNonNull(chunk);
    return new AnalyzeOp(level, chunk);
  }
}
