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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.RevertResult;
import me.moros.gaia.api.operation.GaiaOperation;
import me.moros.gaia.api.platform.Level;
import me.moros.gaia.api.service.ArenaService;
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
    return arenas.containsKey(name) || plugin.coordinator().storage().arenaFileExists(name);
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
      var level = plugin.coordinator().levelService().findLevel(arena.level());
      if (level != null) {
        arena.forEach(c -> plugin.coordinator().operationService().cancel(level, c));
      }
    }
    return plugin.coordinator().storage().deleteArena(name); // Cleanup files
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
    Level level = plugin.coordinator().levelService().findLevel(arena.level());
    if (level == null) {
      return RevertResult.fail(Message.REVERT_ERROR_UNLOADED.build(arena.displayName()));
    } else if (arena.reverting()) {
      return RevertResult.fail(Message.REVERT_ERROR_REVERTING.build(arena.displayName()));
    }
    arena.resetLastReverted();
    long startTime = System.currentTimeMillis();

    arena.chunks().forEach(level::addChunkTicket); // Preload chunks
    var futures = ListUtil.partition(arena.chunks(), 32).stream()
      .map(batch -> plugin.coordinator().storage().loadDataAsync(arena.name(), batch)).toList(); // Load data
    var future = FutureUtil.createFailFastBatch(futures) // Create future
      .thenCompose(batches -> {
        var opFutures = batches.stream().flatMap(Collection::stream)
          .map(data -> GaiaOperation.revert(level, data))
          .map(plugin.coordinator().operationService()::add).toList();
        return FutureUtil.createFailFast(opFutures);
      })
      .handle((ignored, throwable) -> {
        boolean completed = throwable == null;
        long result = System.currentTimeMillis() - startTime;
        plugin.coordinator().eventBus().postArenaRevertEvent(arena, result, completed);
        return completed ? OptionalLong.of(result) : OptionalLong.empty();
      });
    return RevertResult.success(Message.REVERT_SUCCESS.build(arena.displayName()), future);
  }
}
