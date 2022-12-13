/*
 * Copyright 2020-2022 Moros
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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import me.moros.gaia.LightFixer.Mode;
import me.moros.gaia.api.GaiaChunkPos;
import me.moros.gaia.event.ArenaRevertEvent;
import me.moros.gaia.event.ChunkRevertEvent;
import me.moros.gaia.util.Util;

public class RevertListener {
  private final Gaia plugin;
  private final LightFixer fixer;

  RevertListener(Gaia plugin, LightFixer fixer) {
    this.plugin = plugin;
    this.fixer = fixer;
    WorldEdit.getInstance().getEventBus().register(this);
  }

  @Subscribe
  public void onArenaRevert(ArenaRevertEvent event) {
    if (plugin.configManager().config().lightFixer() == Mode.POST_ARENA) {
      handleRevert(event.arena().worldUID(), Util.spiralChunks(event.arena().region()));
    }
  }

  @Subscribe
  public void onChunkRevert(ChunkRevertEvent event) {
    if (plugin.configManager().config().lightFixer() == Mode.POST_CHUNK) {
      handleRevert(event.chunk().parent().worldUID(), List.of(event.chunk()));
    }
  }

  private void handleRevert(UUID world, Collection<GaiaChunkPos> chunks) {
    if (plugin.getServer().isPrimaryThread()) {
      fixer.accept(world, chunks);
    } else {
      plugin.getServer().getScheduler().runTask(plugin, () -> fixer.accept(world, chunks));
    }
  }
}
