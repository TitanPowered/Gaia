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

package me.moros.gaia.locale;

import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import org.checkerframework.checker.nullness.qual.NonNull;

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
  Args2<Component, String> FINISHED_REVERT = (arena, time) -> brand(translatable("gaia.command.revert.finished", GREEN)
    .args(arena, text(time, GREEN)));

  Args1<Component> CANCEL_FAIL = arena -> brand(translatable("gaia.command.cancel.fail", RED)
    .args(arena));
  Args1<Component> CANCEL_SUCCESS = arena -> brand(translatable("gaia.command.cancel.success", YELLOW)
    .args(arena));

  Args2<String, String> VERSION_COMMAND_HOVER = (author, link) -> translatable("gaia.command.version.hover", AQUA)
    .args(text(author, DARK_AQUA), text(link, LINK_COLOR), text("GPLv3", BLUE))
    .append(newline()).append(newline())
    .append(translatable("gaia.command.version.hover.open-link", GRAY));

  static Component brand(ComponentLike message) {
    return PREFIX.asComponent().append(message);
  }

  interface Args0 {
    @NonNull Component build();

    default void send(@NonNull GaiaUser user) {
      if (user.hasLocale()) {
        user.sendMessage(build());
      } else {
        user.sendMessage(GlobalTranslator.render(build(), TranslationManager.DEFAULT_LOCALE));
      }
    }
  }

  interface Args1<A0> {
    @NonNull Component build(@NonNull A0 arg0);

    default void send(@NonNull GaiaUser user, @NonNull A0 arg0) {
      if (user.hasLocale()) {
        user.sendMessage(build(arg0));
      } else {
        user.sendMessage(GlobalTranslator.render(build(arg0), TranslationManager.DEFAULT_LOCALE));
      }
    }
  }

  interface Args2<A0, A1> {
    @NonNull Component build(@NonNull A0 arg0, @NonNull A1 arg1);

    default void send(@NonNull GaiaUser user, @NonNull A0 arg0, @NonNull A1 arg1) {
      if (user.hasLocale()) {
        user.sendMessage(build(arg0, arg1));
      } else {
        user.sendMessage(GlobalTranslator.render(build(arg0, arg1), TranslationManager.DEFAULT_LOCALE));
      }
    }
  }
}
