/*
 *   Copyright 2020-2021 Moros <https://github.com/PrimordialMoros>
 *
 *    This file is part of Gaia.
 *
 *    Gaia is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Gaia is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Gaia.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.gaia;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.configuration.ConfigManager;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.util.functional.AnalyzeOperation;
import me.moros.gaia.util.functional.GaiaOperation;
import me.moros.gaia.util.functional.RevertOperation;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BukkitChunkManager implements ChunkManager {
  private final Map<GaiaChunk, GaiaOperation> tasks;
  private final ConcurrentLinkedQueue<GaiaChunk> queue;
  private final int concurrentChunks;
  private final int concurrentTransactions;

  protected BukkitChunkManager() {
    concurrentChunks = ConfigManager.INSTANCE.getConcurrentChunks();
    concurrentTransactions = ConfigManager.INSTANCE.getConcurrentTransactions();
    tasks = new ConcurrentHashMap<>(concurrentChunks);
    queue = new ConcurrentLinkedQueue<>();
    Bukkit.getScheduler().runTaskTimer(Gaia.getPlugin(), this::processTasks, 0, 1);
  }

  private void processTasks() {
    if (queue.isEmpty() && tasks.isEmpty()) {
      return;
    }
    Iterator<GaiaChunk> it = queue.iterator();
    int counter = 0;
    while (counter < concurrentChunks && it.hasNext()) {
      GaiaChunk chunk = it.next();
      GaiaOperation operation = tasks.get(chunk);
      if (operation != null) {
        runAfterChunkLoad(chunk, () -> {
          GaiaOperation newOperation = operation.process(concurrentTransactions);
          if (newOperation != null) {
            tasks.replace(chunk, operation, newOperation);
          } else {
            tasks.remove(chunk);
          }
        });
        counter++;
      } else {
        tasks.remove(chunk);
        it.remove();
      }
    }
  }

  @Override
  public void shutdown() {
    tasks.clear();
    queue.clear();
  }

  @Override
  public int getRemainingTasks() {
    return tasks.size();
  }

  @Override
  public void cancelTasks(@NonNull GaiaChunk chunk) {
    tasks.remove(chunk);
    queue.removeIf(chunk::equals);
  }

  @Override
  public void revertChunk(@NonNull GaiaChunk chunk, @NonNull World world) {
    if (chunk.isReverting()) {
      return;
    }
    chunk.startReverting();
    GaiaIO.getInstance().loadData(chunk).thenAccept(gd -> {
      if (gd != null) {
        GaiaOperation operation = RevertOperation.create(chunk, gd);
        addInitialOperation(chunk, operation);
      }
    });
  }

  @Override
  public void analyzeChunk(@NonNull GaiaChunk chunk, @NonNull World world) {
    if (chunk.isAnalyzed()) {
      return;
    }
    GaiaOperation operation = AnalyzeOperation.create(chunk);
    addInitialOperation(chunk, operation);
  }

  private void addInitialOperation(final GaiaChunk chunk, final GaiaOperation operation) {
    tasks.put(chunk, operation);
    queue.offer(chunk);
  }

  private void runAfterChunkLoad(GaiaChunk chunk, Runnable runnable) {
    BukkitAdapter.adapt(chunk.getParent().getWorld()).getChunkAtAsync(chunk.getX(), chunk.getZ())
    .thenRun(runnable);
  }
}
