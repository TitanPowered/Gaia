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

package me.moros.gaia.locale;

import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * @see TranslationManager
 */
public interface Message {
  TextColor LINK_COLOR = TextColor.fromHexString("#F5DEB3");

  Component PREFIX = text("[", DARK_GRAY)
    .append(text("Gaia", DARK_AQUA))
    .append(text("] ", DARK_GRAY));

  Args1<Component> CREATE_ANALYZING = arena -> brand(translatable("gaia.command.create.analyzing", GREEN)
    .args(arena));
  Args1<Component> CREATE_FAIL = arena -> brand(translatable("gaia.command.create.fail", RED)
    .args(arena));
  Args1<Component> CREATE_FAIL_TIMEOUT = arena -> brand(translatable("gaia.command.create.fail-timeout", RED)
    .args(arena));
  Args1<Component> CREATE_SUCCESS = arena -> brand(translatable("gaia.command.create.success", GREEN)
    .args(arena));

  Args0 PLAYER_REQUIRED = () -> brand(translatable("gaia.command.create.error.player-required", RED));
  Args0 CREATE_ERROR_VALIDATION = () -> brand(translatable("gaia.command.create.error.validation", RED));
  Args1<String> CREATE_ERROR_EXISTS = arena -> brand(translatable("gaia.command.create.error.exists", RED)
    .args(text(arena, GOLD)));
  Args0 CREATE_ERROR_SELECTION = () -> brand(translatable("gaia.command.create.error.selection", RED));
  Args0 CREATE_ERROR_CUBOID = () -> brand(translatable("gaia.command.create.error.cuboid", RED));
  Args0 CREATE_ERROR_SIZE = () -> brand(translatable("gaia.command.create.error.size", RED));
  Args0 CREATE_ERROR_DISTANCE = () -> brand(translatable("gaia.command.create.error.distance", RED));
  Args0 CREATE_ERROR_INTERSECTION = () -> brand(translatable("gaia.command.create.error.intersection", RED));
  Args0 CREATE_ERROR_CRITICAL = () -> brand(translatable("gaia.command.create.error.critical", RED));

  Args0 LIST_NOT_FOUND = () -> brand(translatable("gaia.command.list.not-found", YELLOW));
  Args0 LIST_INVALID_PAGE = () -> brand(translatable("gaia.command.list.invalid-page", RED));

  Args1<String> REMOVE_FAIL = arena -> brand(translatable("gaia.command.remove.fail", RED)
    .args(text(arena, GOLD)));
  Args1<String> REMOVE_SUCCESS = arena -> brand(translatable("gaia.command.remove.success", GREEN)
    .args(text(arena, GOLD)));

  Args1<Component> REVERT_SUCCESS = arena -> brand(translatable("gaia.command.revert.success", GREEN)
    .args(arena));
  Args1<Component> REVERT_ERROR_ANALYZING = arena -> brand(translatable("gaia.command.revert.error.not-analyzed", YELLOW)
    .args(arena));
  Args1<Component> REVERT_ERROR_REVERTING = arena -> brand(translatable("gaia.command.revert.error.already-reverting", YELLOW)
    .args(arena));
  Args1<Long> REVERT_COOLDOWN = time -> brand(translatable("gaia.command.revert.on-cooldown", DARK_AQUA)
    .args(text(Util.formatDuration(time), YELLOW)));
  Args2<Component, Long> FINISHED_REVERT = (arena, time) -> brand(translatable("gaia.command.revert.finished", GREEN)
    .args(arena, text(time, GREEN)));

  Args1<Component> CANCEL_FAIL = arena -> brand(translatable("gaia.command.cancel.fail", RED)
    .args(arena));
  Args1<Component> CANCEL_SUCCESS = arena -> brand(translatable("gaia.command.cancel.success", YELLOW)
    .args(arena));

  Args2<String, String> VERSION_COMMAND_HOVER = (author, link) -> translatable("gaia.command.version.hover", AQUA)
    .args(text(author, DARK_AQUA), text(link, LINK_COLOR), text("GPLv3", BLUE))
    .append(newline()).append(newline())
    .append(translatable("gaia.command.version.hover.open-link", GRAY));
  Args0 CONFIG_RELOAD = () -> translatable("gaia.command.config-reload", GREEN);

  Args1<Component> NO_POINTS = arena -> brand(translatable("gaia.command.point.empty", YELLOW)
    .args(arena));
  Args1<Component> LIST_POINTS = arena -> brand(translatable("gaia.command.point.header", YELLOW)
    .args(arena));
  Args0 INVALID_POINT = () -> brand(translatable("gaia.command.point.list.invalid-point", RED));
  Args0 ADD_POINT_SUCCESS = () -> brand(translatable("gaia.command.point.add.success", GREEN));
  Args0 ADD_POINT_FAIL = () -> brand(translatable("gaia.command.point.add.fail", RED));
  Args0 ADD_POINT_FAIL_AREA = () -> brand(translatable("gaia.command.point.add.fail.area", YELLOW));
  Args1<Component> CLEAR_POINTS = arena -> brand(translatable("gaia.command.point.clear", GREEN)
    .args(arena));

  static Component brand(ComponentLike message) {
    return PREFIX.asComponent().append(message);
  }

  interface Args0 {
    Component build();

    default void send(GaiaUser user) {
      user.sendMessage(build());
    }
  }

  interface Args1<A0> {
    Component build(A0 arg0);

    default void send(GaiaUser user, A0 arg0) {
      user.sendMessage(build(arg0));
    }
  }

  interface Args2<A0, A1> {
    Component build(A0 arg0, A1 arg1);

    default void send(GaiaUser user, A0 arg0, A1 arg1) {
      user.sendMessage(build(arg0, arg1));
    }
  }
}
