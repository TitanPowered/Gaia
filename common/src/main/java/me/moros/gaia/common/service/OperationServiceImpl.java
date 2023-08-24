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

package me.moros.gaia.common.service;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.api.operation.GaiaOperation;
import me.moros.gaia.api.operation.GaiaOperation.Result;
import me.moros.gaia.api.platform.Level;
import me.moros.gaia.api.service.OperationService;
import me.moros.gaia.common.config.Config;
import me.moros.gaia.common.config.ConfigManager;
import me.moros.tasker.executor.SyncExecutor;

public final class OperationServiceImpl implements OperationService {
  private final Gaia plugin;
  private final Queue<GaiaOperation.ChunkOperation<?>> queue;

  private final AtomicBoolean updatedConfig;
  private Config configSnapshot;
  private boolean valid;

  public OperationServiceImpl(Gaia plugin, SyncExecutor syncExecutor) {
    this.plugin = plugin;
    this.queue = new ConcurrentLinkedQueue<>();
    this.updatedConfig = new AtomicBoolean();
    refreshConfig();
    ConfigManager.instance().subscribe(n -> updatedConfig.set(true));
    syncExecutor.repeat(this::processTasks, 1, 1);
    this.valid = true;
  }

  private void refreshConfig() {
    configSnapshot = ConfigManager.instance().config();
  }

  private void processTasks() {
    if (!valid) {
      return;
    }
    if (queue.isEmpty()) {
      if (updatedConfig.compareAndSet(true, false)) {
        refreshConfig();
      }
      return;
    }
    long startTime = System.currentTimeMillis();
    var it = queue.iterator();
    int counter = 0;
    while (counter < configSnapshot.concurrentChunks() && it.hasNext()) {
      var operation = it.next();
      Result result = operation.update();
      if (result == Result.REMOVE) {
        onComplete(operation);
        it.remove();
      }
      if (result != Result.WAIT) {
        counter++;
      }
      if (System.currentTimeMillis() > startTime + 30) {
        break;
      }
    }
  }

  @Override
  public void shutdown() {
    valid = false;
    queue.forEach(op -> {
      op.asFuture().cancel(false);
      cleanupChunk(op.level(), op);
    });
    queue.clear();
  }

  @Override
  public int remainingOperations() {
    return queue.size();
  }

  @Override
  public <T> CompletableFuture<T> add(GaiaOperation.ChunkOperation<T> operation) {
    if (valid) {
      operation.level().loadChunkWithTicket(operation.x(), operation.z()).thenAccept(ignore -> queue.offer(operation));
      return operation.asFuture();
    }
    return CompletableFuture.failedFuture(new RuntimeException("Unable to queue operation!"));
  }

  private void onComplete(GaiaOperation.ChunkOperation<?> op) {
    if (op.asFuture().isDone() && !op.asFuture().isCompletedExceptionally()) {
      long deltaTime = System.currentTimeMillis() - op.startTime();
      if (op instanceof GaiaOperation.Analyze) {
        plugin.eventBus().postChunkAnalyzeEvent(op.chunk(), op.level().key(), deltaTime);
      } else if (op instanceof GaiaOperation.Revert) {
        plugin.eventBus().postChunkRevertEvent(op.chunk(), op.level().key(), deltaTime);
      }
    }
    cleanupChunk(op.level(), op);
  }

  @Override
  public void cancel(Level level, ChunkRegion chunk) {
    queue.removeIf(op -> cancelMatching(op, level, chunk));
    cleanupChunk(level, chunk);
  }

  private boolean cancelMatching(GaiaOperation.ChunkOperation<?> op, Level level, ChunkPosition pos) {
    if (op.level().key().equals(level.key()) && op.x() == pos.x() && op.z() == pos.z()) {
      op.asFuture().cancel(false);
      return true;
    }
    return false;
  }

  private void cleanupChunk(Level level, ChunkPosition position) {
    level.removeChunkTicket(position.x(), position.z());
  }
}
