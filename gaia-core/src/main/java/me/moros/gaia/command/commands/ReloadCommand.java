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

package me.moros.gaia.command.commands;

import cloud.commandframework.meta.CommandMeta;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.command.CommandPermissions;
import me.moros.gaia.command.Commander;
import me.moros.gaia.command.GaiaCommand;
import me.moros.gaia.locale.Message;

public record ReloadCommand(Commander commander) implements GaiaCommand {
  @Override
  public void register() {
    var builder = commander().rootBuilder();
    commander().register(builder.literal("reload", "rel")
      .meta(CommandMeta.DESCRIPTION, "Reload the plugin and its config")
      .permission(CommandPermissions.RELOAD)
      .handler(c -> onReload(c.getSender()))
    );
  }

  private void onReload(GaiaUser user) {
    commander().plugin().translationManager().reload();
    Message.CONFIG_RELOAD.send(user);
  }
}
