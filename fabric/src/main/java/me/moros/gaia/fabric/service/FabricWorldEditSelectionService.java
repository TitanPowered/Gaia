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

package me.moros.gaia.fabric.service;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.fabric.FabricAdapter;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.common.service.WorldEditSelectionService;
import net.kyori.adventure.identity.Identity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class FabricWorldEditSelectionService extends WorldEditSelectionService {
  private final PlayerList playerList;

  public FabricWorldEditSelectionService(MinecraftServer server) {
    this.playerList = server.getPlayerList();
  }

  @Override
  protected @Nullable Player adapt(GaiaUser user) {
    return user.get(Identity.UUID).map(playerList::getPlayer).map(FabricAdapter::adaptPlayer).orElse(null);
  }
}
