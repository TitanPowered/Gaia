/*
 * Copyright 2020-2026 Moros
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

package me.moros.gaia.paper.service;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.api.service.UserService;
import me.moros.gaia.paper.platform.BukkitGaiaUser;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.Nullable;

public record UserServiceImpl(Supplier<Gaia> supplier, Server server) implements UserService {
  @Override
  public @Nullable GaiaUser findUser(UUID uuid) {
    var player = server().getPlayer(uuid);
    return player == null ? null : BukkitGaiaUser.from(supplier().get(), player);
  }

  @Override
  public @Nullable GaiaUser findUser(String input) {
    var player = server().getPlayer(input);
    if (player == null) {
      try {
        UUID uuid = UUID.fromString(input);
        player = server().getPlayer(uuid);
      } catch (IllegalArgumentException ignore) {
      }
    }
    return player == null ? null : BukkitGaiaUser.from(supplier().get(), player);
  }

  @Override
  public Stream<String> users() {
    return server().getOnlinePlayers().stream().map(Entity::getName);
  }
}
