/*
 * Copyright 2020-2024 Moros
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

package me.moros.gaia.common.command;

import java.util.stream.Stream;

import org.incendo.cloud.permission.Permission;

public final class CommandPermissions {
  private CommandPermissions() {
  }

  public static final Permission HELP = create("help");
  public static final Permission LIST = create("list");
  public static final Permission INFO = create("info");
  public static final Permission VERSION = create("version");
  public static final Permission CREATE = create("create");
  public static final Permission REMOVE = create("remove");
  public static final Permission REVERT = create("revert");
  public static final Permission BYPASS = create("bypass");
  public static final Permission POINT = create("point");
  public static final Permission TELEPORT = create("teleport");

  private static Permission create(String node) {
    return Permission.of("gaia.command." + node);
  }

  public static Stream<Permission> adminOnly() {
    return Stream.of(HELP, LIST, INFO, VERSION, CREATE, REMOVE, REVERT, BYPASS, POINT, TELEPORT);
  }
}
