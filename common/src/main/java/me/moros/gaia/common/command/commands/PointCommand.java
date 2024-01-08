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

package me.moros.gaia.common.command.commands;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.meta.CommandMeta;
import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.common.command.CommandPermissions;
import me.moros.gaia.common.command.Commander;
import me.moros.gaia.common.command.GaiaCommand;
import me.moros.gaia.common.command.argument.ArenaArgument;
import me.moros.gaia.common.locale.Message;

public record PointCommand(Commander commander) implements GaiaCommand {
  @Override
  public void register() {
    var arenaArg = ArenaArgument.builder("arena").asOptionalWithDefault("cur");
    var builder = commander().rootBuilder();
    commander().register(builder.literal("addpoint", "add", "a")
      .meta(CommandMeta.DESCRIPTION, "Add a new point")
      .permission(CommandPermissions.POINT)
      .handler(c -> onPointAdd(c.getSender()))
    );
    commander().register(builder.literal("clearpoints", "clearpoint", "clear")
      .meta(CommandMeta.DESCRIPTION, "Clear all points for the specified arena")
      .permission(CommandPermissions.POINT)
      .argument(arenaArg.build())
      .handler(c -> onPointClear(c.getSender(), c.get("arena")))
    );
    commander().register(builder.literal("teleport", "tp")
      .meta(CommandMeta.DESCRIPTION, "Teleport to a point in the specified arena")
      .permission(CommandPermissions.TELEPORT)
      .argument(arenaArg.build())
      .argument(IntegerArgument.optional("id", 0))
      .handler(c -> onPointTeleport(c.getSender(), c.get("arena"), c.get("id")))
    );
  }

  private void onPointAdd(GaiaUser user) {
    Arena arena = user.level().flatMap(l -> user.parent().arenaService().arena(l, user.position()))
      .orElse(null);
    if (arena == null) {
      Message.ADD_POINT_FAIL_AREA.send(user);
      return;
    }
    Point point = user.createPoint().orElse(null);
    if (point != null) {
      arena.addPoint(point);
      user.parent().storage().saveArena(arena);
      Message.ADD_POINT_SUCCESS.send(user);
    } else {
      Message.ADD_POINT_FAIL.send(user);
    }
  }

  private void onPointClear(GaiaUser user, Arena arena) {
    arena.clearPoints();
    user.parent().storage().saveArena(arena);
    Message.CLEAR_POINTS.send(user, arena.displayName());
  }

  private void onPointTeleport(GaiaUser user, Arena arena, Integer id) {
    List<Point> points = arena.points();
    if (points.isEmpty()) {
      Message.NO_POINTS.send(user, arena.displayName());
      return;
    }
    if (id < 0 || id > points.size()) {
      Message.INVALID_POINT.send(user);
      return;
    }
    Point pointToTeleport = points.get(id == 0 ? ThreadLocalRandom.current().nextInt(points.size()) : id - 1);
    user.teleport(arena.level(), pointToTeleport);
  }
}
