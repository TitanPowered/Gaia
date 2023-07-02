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

import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.api.config.Config;
import me.moros.gaia.api.config.ConfigManager;
import me.moros.gaia.api.operation.GaiaOperation;
import me.moros.gaia.api.operation.GaiaOperation.Result;
import me.moros.gaia.api.platform.Level;
import me.moros.gaia.api.region.ChunkRegion;
import me.moros.gaia.api.service.OperationService;
import me.moros.tasker.executor.SyncExecutor;

public final class OperationServiceImpl implements OperationService {
  private final ConfigManager configManager;
  private final Queue<GaiaOperation.ChunkOperation<?>> queue;

  private final AtomicBoolean updatedConfig;
  private Limits limits;
  private boolean valid;

  public OperationServiceImpl(ConfigManager configManager, SyncExecutor syncExecutor) {
    this.configManager = configManager;
    this.queue = new ConcurrentLinkedQueue<>();
    this.updatedConfig = new AtomicBoolean();
    this.limits = setupLimits();
    this.configManager.subscribe(n -> updatedConfig.set(true));
    syncExecutor.repeat(this::processTasks, 1, 1);
    this.valid = true;
  }

  private Limits setupLimits() {
    Config c = configManager.config();
    return new Limits(c.concurrentChunks(), c.concurrentTransactions());
  }

  private void processTasks() {
    if (!valid) {
      return;
    }
    if (queue.isEmpty()) {
      if (updatedConfig.compareAndSet(true, false)) {
        limits = setupLimits();
      }
      return;
    }
    var it = queue.iterator();
    int counter = 0;
    while (counter < limits.concurrentChunks() && it.hasNext()) {
      var operation = it.next();
      Result result = operation.update(limits.concurrentTransactions);
      if (result == Result.REMOVE) {
        cleanupTicket(operation);
        it.remove();
      } else {
        ++counter;
      }
    }
  }

  @Override
  public void shutdown() {
    valid = false;
    queue.forEach(op -> {
      op.asFuture().cancel(false);
      cleanupTicket(op);
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
      queue.offer(operation);
      return operation.asFuture();
    }
    return CompletableFuture.failedFuture(new RuntimeException("Unable to queue operation!"));
  }

  @Override
  public void cancel(Level level, ChunkRegion.Validated chunk) {
    queue.removeIf(op -> cancelMatching(op, level, chunk));
    level.removeChunkTicket(chunk);
  }

  private record Limits(int concurrentChunks, int concurrentTransactions) {
  }

  private boolean cancelMatching(GaiaOperation.ChunkOperation<?> op, Level level, ChunkPosition pos) {
    if (op.level().key().equals(level.key()) && op.x() == pos.x() && op.z() == pos.z()) {
      op.asFuture().cancel(false);
      return true;
    }
    return false;
  }

  private void cleanupTicket(GaiaOperation.ChunkOperation<?> op) {
    op.level().removeChunkTicket(op.x(), op.z());
  }
}
