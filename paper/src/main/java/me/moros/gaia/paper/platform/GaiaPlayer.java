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

package me.moros.gaia.paper.platform;

import java.util.Optional;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.paper.platform.BukkitGaiaUser.BukkitGaiaPlayer;
import me.moros.gaia.paper.platform.GaiaPlayer.GaiaPlayerImpl;
import me.moros.math.Vector3d;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.entity.Player;

sealed interface GaiaPlayer extends GaiaUser permits BukkitGaiaPlayer, GaiaPlayerImpl {
  Player player();

  @Override
  default Audience audience() {
    return player();
  }

  @Override
  default boolean isPlayer() {
    return true;
  }

  @Override
  default void teleport(Key worldKey, Point point) {
    var world = player().getServer().getWorld(worldKey);
    if (world != null) {
      player().teleportAsync(new Location(world, point.x(), point.y(), point.z(), point.yaw(), point.pitch()));
    }
  }

  @Override
  default Optional<Key> level() {
    return Optional.of(player().getWorld().key());
  }

  @Override
  default Vector3d position() {
    var pos = player().getLocation();
    return Vector3d.of(pos.getX(), pos.getY(), pos.getZ());
  }

  @Override
  default float yaw() {
    return player().getYaw();
  }

  @Override
  default float pitch() {
    return player().getPitch();
  }

  record GaiaPlayerImpl(Gaia parent, Player player) implements GaiaPlayer {
  }
}
