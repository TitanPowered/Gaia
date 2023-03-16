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

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sk89q.worldedit.world.World;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaData;
import me.moros.gaia.config.Config;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.util.functional.AnalyzeOperation;
import me.moros.gaia.util.functional.GaiaOperation;
import me.moros.gaia.util.functional.RevertOperation;

public abstract class SimpleChunkManager implements ChunkManager {
  private final GaiaPlugin plugin;
  private final ConcurrentLinkedQueue<OperationEntry> queue;

  private Limits limits;
  private final AtomicBoolean updatedConfig = new AtomicBoolean(false);

  protected SimpleChunkManager(GaiaPlugin plugin) {
    this.plugin = plugin;
    limits = setupLimits();
    this.plugin.configManager().subscribe(n -> updatedConfig.set(true));
    queue = new ConcurrentLinkedQueue<>();
    plugin.repeat(this::processTasks, 0, 1);
  }

  private Limits setupLimits() {
    Config c = plugin.configManager().config();
    return new Limits(c.concurrentChunks(), c.concurrentTransactions());
  }

  private void processTasks() {
    if (queue.isEmpty()) {
      if (updatedConfig.compareAndSet(true, false)) {
        limits = setupLimits();
      }
      return;
    }
    Iterator<OperationEntry> it = queue.iterator();
    int counter = 0;
    while (counter < limits.concurrentChunks() && it.hasNext()) {
      OperationEntry entry = it.next();
      if (entry.operation().update(limits.concurrentTransactions)) {
        onChunkOperationComplete(entry.chunk());
        it.remove();
      } else {
        counter++;
      }
    }
  }

  @Override
  public void shutdown() {
    queue.forEach(e -> onChunkOperationComplete(e.chunk()));
    queue.clear();
  }

  @Override
  public int remainingTasks() {
    return queue.size();
  }

  @Override
  public void cancel(GaiaChunk chunk) {
    queue.removeIf(op -> chunk.equals(op.chunk()));
    onChunkOperationComplete(chunk);
  }

  @Override
  public void revert(GaiaChunk chunk, World world) {
    if (chunk.reverting()) {
      return;
    }
    chunk.startReverting();
    CompletableFuture<GaiaData> dataLoad = GaiaIO.instance().loadData(chunk);
    CompletableFuture.allOf(asyncLoad(chunk), dataLoad).thenAccept(x -> {
      GaiaData gd = dataLoad.join();
      if (gd != null) {
        addInitialOperation(chunk, RevertOperation.create(chunk, gd));
      } else {
        throw new CompletionException(new NullPointerException("Null chunk data"));
      }
    });
  }

  @Override
  public void analyze(GaiaChunk chunk, World world) {
    if (chunk.analyzed()) {
      return;
    }
    asyncLoad(chunk).thenAccept(c -> addInitialOperation(chunk, AnalyzeOperation.create(chunk)));
  }

  private void addInitialOperation(final GaiaChunk chunk, final GaiaOperation operation) {
    queue.offer(new OperationEntry(chunk, operation));
  }

  private record Limits(int concurrentChunks, int concurrentTransactions) {
  }

  private record OperationEntry(GaiaChunk chunk, GaiaOperation operation) {
  }
}
