/*
 * Copyright 2020-2021 Moros
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

import me.moros.gaia.api.GaiaUser;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BukkitGaiaUser implements GaiaUser {
  private final CommandSender sender;
  private final boolean isPlayer;

  public BukkitGaiaUser(@NonNull CommandSender sender) {
    this.sender = sender;
    isPlayer = sender instanceof Player;
  }

  public @NonNull CommandSender sender() {
    return this.sender;
  }

  @Override
  public @NonNull String name() {
    return sender.getName();
  }

  @Override
  public boolean isPlayer() {
    return isPlayer;
  }

  @Override
  public boolean hasLocale() {
    return isPlayer;
  }

  @Override
  public boolean hasPermission(@NonNull String permission) {
    return sender.hasPermission(permission);
  }

  @Override
  public @NonNull Audience audience() {
    return sender;
  }
}
