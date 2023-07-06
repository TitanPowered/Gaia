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

package me.moros.gaia.paper.platform;

import java.util.Optional;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.common.platform.AbstractUser;
import me.moros.math.Vector3d;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitGaiaUser extends AbstractUser<CommandSender> {
  private BukkitGaiaUser(Gaia parent, CommandSender handle) {
    super(parent, handle);
  }

  public static final class BukkitGaiaPlayer extends BukkitGaiaUser {
    private final Player player;

    private BukkitGaiaPlayer(Gaia plugin, Player player) {
      super(plugin, player);
      this.player = player;
    }

    @Override
    public boolean isPlayer() {
      return true;
    }

    @Override
    public void teleport(Key worldKey, Point point) {
      var world = player.getServer().getWorld(new NamespacedKey(worldKey.namespace(), worldKey.value()));
      if (world != null) {
        player.teleportAsync(new Location(world, point.x(), point.y(), point.z(), point.yaw(), point.pitch()));
      }
    }

    @Override
    public Optional<Key> level() {
      return Optional.of(player.getWorld().key());
    }

    @Override
    public Vector3d position() {
      var pos = player.getLocation();
      return Vector3d.of(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public float yaw() {
      return player.getLocation().getYaw();
    }

    @Override
    public float pitch() {
      return player.getLocation().getPitch();
    }
  }

  public static GaiaUser from(Gaia parent, CommandSender sender) {
    if (sender instanceof Player player) {
      return new BukkitGaiaPlayer(parent, player);
    }
    return new BukkitGaiaUser(parent, sender);
  }
}
