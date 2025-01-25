/*
 * Copyright 2020-2025 Moros
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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.RevertResult;
import me.moros.gaia.api.operation.GaiaOperation;
import me.moros.gaia.api.platform.Level;
import me.moros.gaia.api.service.ArenaService;
import me.moros.gaia.common.config.ConfigManager;
import me.moros.gaia.common.locale.Message;
import me.moros.gaia.common.util.FutureUtil;
import me.moros.gaia.common.util.ListUtil;

public class ArenaServiceImpl implements ArenaService {
  private final Gaia plugin;

  private final Map<String, Arena> arenas;

  public ArenaServiceImpl(Gaia plugin) {
    this.plugin = plugin;
    this.arenas = new ConcurrentHashMap<>();
  }

  @Override
  public boolean contains(String name) {
    return arenas.containsKey(name) || plugin.storage().arenaFileExists(name);
  }

  @Override
  public Optional<Arena> arena(String name) {
    return Optional.ofNullable(arenas.get(name));
  }

  @Override
  public boolean add(Arena arena) {
    if (stream().filter(a -> a.level().equals(arena.level())).map(Arena::region).anyMatch(arena.region()::intersects)) {
      return false;
    }
    return arenas.putIfAbsent(arena.name(), arena) == null;
  }

  @Override
  public boolean remove(String name) {
    Arena arena = arenas.remove(name);
    if (arena != null) {
      var level = plugin.levelService().findLevel(arena.level());
      if (level != null) {
        arena.forEach(c -> plugin.operationService().cancel(level, c));
      }
    }
    return plugin.storage().deleteArena(name); // Cleanup files
  }

  @Override
  public int size() {
    return arenas.size();
  }

  @Override
  public Stream<Arena> stream() {
    return arenas.values().stream();
  }

  @Override
  public Iterator<Arena> iterator() {
    return Collections.unmodifiableCollection(arenas.values()).iterator();
  }

  @Override
  public RevertResult revert(Arena arena) {
    Level level = plugin.levelService().findLevel(arena.level());
    if (level == null) {
      return RevertResult.fail(Message.REVERT_ERROR_UNLOADED.build(arena.displayName()));
    } else if (arena.reverting()) {
      return RevertResult.fail(Message.REVERT_ERROR_REVERTING.build(arena.displayName()));
    }
    arena.resetLastReverted();
    long startTime = System.currentTimeMillis();
    final var chunks = arena.chunks();
    for (var chunk : chunks) {
      level.addChunkTicket(chunk); // Preload chunks
      chunk.reverting(true); // Set flag per chunk
    }
    var futures = ListUtil.partition(chunks, 32).stream()
      .map(batch -> plugin.storage().loadDataAsync(arena.name(), batch)).toList(); // Load data
    final int sectionsPerTick = ConfigManager.instance().config().sectionsPerTick();
    var future = FutureUtil.createFailFastBatch(futures) // Create future
      .thenCompose(batches -> {
        var opFutures = batches.stream().flatMap(Collection::stream)
          .map(data -> GaiaOperation.revert(level, data, sectionsPerTick))
          .map(plugin.operationService()::add).toList();
        return FutureUtil.createFailFast(opFutures);
      })
      .handle((ignored, throwable) -> {
        boolean completed = throwable != null;
        long result = System.currentTimeMillis() - startTime;
        plugin.eventBus().postArenaRevertEvent(arena, result, completed);
        if (throwable != null) {
          throw new CompletionException(throwable);
        }
        return result;
      });
    return RevertResult.success(Message.REVERT_SUCCESS.build(arena.displayName()), future);
  }
}
