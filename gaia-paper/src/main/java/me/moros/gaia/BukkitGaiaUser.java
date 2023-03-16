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

package me.moros.gaia;

import java.util.UUID;

import me.moros.gaia.api.GaiaUser;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public record BukkitGaiaUser(GaiaPlugin parent, CommandSender sender, boolean isPlayer) implements GaiaUser {
  public BukkitGaiaUser(GaiaPlugin parent, CommandSender sender) {
    this(parent, sender, sender instanceof Player);
  }

  @Override
  public @Nullable UUID worldUUID() {
    return isPlayer ? ((Player) sender).getWorld().getUID() : null;
  }

  @Override
  public boolean hasPermission(String permission) {
    return sender().hasPermission(permission);
  }

  @Override
  public @NonNull Audience audience() {
    return sender();
  }
}
