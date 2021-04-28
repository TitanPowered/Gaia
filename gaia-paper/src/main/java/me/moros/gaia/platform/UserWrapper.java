/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *
 * 	  This file is part of Gaia.
 *
 *    Gaia is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Gaia is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Gaia.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.gaia.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public class UserWrapper implements GaiaUser {
  private final CommandSender sender;

  public UserWrapper(@NonNull CommandSender sender) {
    this.sender = sender;
  }

  public @NonNull CommandSender get() {
    return this.sender;
  }

  @Override
  public @NonNull String getName() {
    return sender.getName();
  }

  @Override
  public boolean hasPermission(@NonNull String permission) {
    return sender.hasPermission(permission);
  }

  @Override
  public void sendMessage(@NonNull Component text) {
    sender.sendMessage(text);
  }
}
