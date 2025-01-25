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
import java.util.List;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.api.event.ArenaRevertEvent;
import me.moros.gaia.api.event.ChunkRevertEvent;
import me.moros.gaia.api.util.ChunkUtil;
import me.moros.gaia.api.util.LightFixer;
import me.moros.gaia.common.config.ConfigManager;
import net.kyori.adventure.key.Key;

public final class RevertListener {
  private final Gaia plugin;

  public RevertListener(Gaia plugin) {
    this.plugin = plugin;
    registerListeners();
  }

  private void registerListeners() {
    this.plugin.eventBus().subscribe(ArenaRevertEvent.class, this::onArenaRevert);
    this.plugin.eventBus().subscribe(ChunkRevertEvent.class, this::onChunkRevert);
  }

  private void onArenaRevert(ArenaRevertEvent event) {
    if (ConfigManager.instance().config().lightFixer() == LightFixer.POST_ARENA) {
      handleRevert(event.arena().level(), ChunkUtil.spiralChunks(event.arena().region()));
    }
  }

  private void onChunkRevert(ChunkRevertEvent event) {
    if (ConfigManager.instance().config().lightFixer() == LightFixer.POST_CHUNK) {
      handleRevert(event.level(), List.of(event.chunk()));
    }
  }

  private void handleRevert(Key levelKey, Collection<ChunkPosition> chunks) {
    var level = plugin.levelService().findLevel(levelKey);
    if (level != null) {
      level.fixLight(chunks);
    }
  }
}
