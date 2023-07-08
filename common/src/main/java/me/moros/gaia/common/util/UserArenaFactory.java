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

package me.moros.gaia.common.util;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.gaia.api.operation.GaiaOperation;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.api.platform.Level;
import me.moros.gaia.api.util.ChunkUtil;
import me.moros.gaia.api.util.TextUtil;
import me.moros.gaia.common.config.ConfigManager;
import me.moros.gaia.common.locale.Message;

public final class UserArenaFactory {
  private final GaiaUser user;

  public UserArenaFactory(GaiaUser user) {
    this.user = user;
  }

  public boolean tryCreate(String name) {
    String arenaName = TextUtil.sanitizeInput(name);
    if (arenaName.length() < 3) {
      Message.CREATE_ERROR_VALIDATION.send(user);
      return false;
    }
    if (user.parent().arenaService().contains(arenaName)) {
      Message.CREATE_ERROR_EXISTS.send(user, arenaName);
      return false;
    }
    return withValidName(arenaName);
  }

  private boolean withValidName(String arenaName) {
    Level level = user.level().map(user.parent().levelService()::findLevel).orElse(null);
    if (level == null) {
      Message.PLAYER_REQUIRED.send(user);
      return false;
    }
    var region = user.parent().selectionService().selection(user).orElse(null);
    if (region == null) {
      Message.CREATE_ERROR_SELECTION.send(user);
      return false;
    }
    double radius = region.size().maxComponent();
    if (radius > 1024) { // For safety reasons limit to maximum 64 chunks in any direction
      Message.CREATE_ERROR_SIZE.send(user);
      return false;
    }
    if (region.center().distanceSq(user.position()) > radius * radius) {
      Message.CREATE_ERROR_DISTANCE.send(user);
      return false;
    }
    if (user.parent().arenaService().arena(level.key(), region).isPresent()) {
      Message.CREATE_ERROR_INTERSECTION.send(user);
      return false;
    }
    user.parent().selectionService().resetSelection(user);
    return withValidSelection(arenaName, level, region);
  }

  private boolean withValidSelection(String arenaName, Level level, Region region) {
    if (!user.parent().storage().createEmptyArenaFiles(arenaName)) {
      Message.CREATE_ERROR_CRITICAL.send(user);
      return false;
    }
    var chunkRegions = ChunkUtil.splitIntoChunks(region);
    if (chunkRegions.isEmpty()) {
      Message.CREATE_FAIL.send(user, arenaName);
      return false;
    }
    var size = region.size();
    int optimalChunkAmount = ChunkUtil.toChunkPos(size.blockX()) * ChunkUtil.toChunkPos(size.blockZ());
    int optimalHeight = ChunkUtil.toChunkPos(size.blockY());
    if (chunkRegions.size() > optimalChunkAmount || ChunkUtil.calculateSections(region) % 16 > optimalHeight) {
      Message.CREATE_WARN_CHUNK_ALIGN.send(user);
    }
    Message.CREATE_ANALYZING.send(user, arenaName);
    createFuture(arenaName, level, region, chunkRegions);
    return true;
  }

  private void createFuture(String arenaName, Level level, Region region, Collection<ChunkRegion> chunkRegions) {
    var futures = chunkRegions.stream().map(c -> GaiaOperation.snapshotAnalyze(level, c))
      .map(user.parent().operationService()::add).toList();
    long startTime = System.currentTimeMillis();
    AtomicLong deltaTime = new AtomicLong();
    FutureUtil.createFailFastBatch(futures)
      .orTimeout(ConfigManager.instance().config().timeout(), TimeUnit.MILLISECONDS)
      .thenCompose(data -> FutureUtil.createFailFastBatch(ListUtil.partition(data, 64).stream()
        .map(batch -> user.parent().storage().saveDataAsync(arenaName, batch)).toList())
      )
      .thenCompose(validatedBatches -> {
        var validated = validatedBatches.stream().flatMap(Collection::stream).toList();
        int expected = chunkRegions.size();
        int result = validated.size();
        if (result != expected) {
          return null;
        }
        var arena = Arena.builder().name(arenaName).level(level.key()).region(region).chunks(validated).build();
        long dt = System.currentTimeMillis() - startTime;
        deltaTime.set(dt);
        user.parent().eventBus().postArenaAnalyzeEvent(arena, dt);
        return user.parent().storage().saveArena(arena);
      })
      .whenComplete((arena, throwable) -> {
        chunkRegions.forEach(level::removeChunkTicket); // Force cleanup
        if (arena != null) {
          user.parent().arenaService().add(arena);
          Message.CREATE_SUCCESS.send(user, arena.displayName(), deltaTime.get());
        } else {
          user.parent().arenaService().remove(arenaName);
          if (throwable instanceof TimeoutException) {
            Message.CREATE_FAIL_TIMEOUT.send(user, arenaName);
          } else {
            Message.CREATE_FAIL.send(user, arenaName);
            throwable.printStackTrace();
          }
        }
      });
  }
}
