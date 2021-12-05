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

package me.moros.gaia.command;

import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.Permission;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandPermissions {
  private CommandPermissions() {
  }

  public static CommandPermission HELP = create("help");
  public static CommandPermission LIST = create("list");
  public static CommandPermission INFO = create("info");
  public static CommandPermission CREATE = create("create");
  public static CommandPermission REMOVE = create("remove");
  public static CommandPermission REVERT = create("revert");
  public static CommandPermission CANCEL = create("cancel");
  public static CommandPermission VERSION = create("version");

  private static Permission create(@NonNull String node) {
    return Permission.of("gaia.command." + node);
  }
}
