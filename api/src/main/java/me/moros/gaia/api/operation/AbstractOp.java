/*
 * Copyright 2020-2026 Moros
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

import java.util.concurrent.CompletableFuture;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.platform.Level;

public abstract class AbstractOp<T> implements GaiaOperation<T> {
  protected final long startTime;
  protected final CompletableFuture<T> future;

  protected AbstractOp() {
    this.startTime = System.currentTimeMillis();
    this.future = new CompletableFuture<>();
  }

  @Override
  public final long startTime() {
    return startTime;
  }

  @Override
  public final Result update() {
    if (future.isDone()) {
      return Result.REMOVE;
    }
    return processStep();
  }

  @Override
  public final CompletableFuture<T> asFuture() {
    return future;
  }

  protected abstract Result processStep();

  static abstract class LevelChunkOp<T> extends AbstractOp<T> implements GaiaOperation.ChunkOperation<T> {
    protected final Level level;
    protected final ChunkRegion chunk;

    protected LevelChunkOp(Level level, ChunkRegion chunk) {
      this.level = level;
      this.chunk = chunk;
    }

    @Override
    public ChunkRegion chunk() {
      return chunk;
    }

    @Override
    public Level level() {
      return level;
    }

    @Override
    public final int x() {
      return chunk.x();
    }

    @Override
    public final int z() {
      return chunk.z();
    }
  }
}
