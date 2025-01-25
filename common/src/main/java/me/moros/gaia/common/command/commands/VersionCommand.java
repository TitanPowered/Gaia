/*
 * Copyright 2020-2025 Moros
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

import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.common.command.CommandPermissions;
import me.moros.gaia.common.command.Commander;
import me.moros.gaia.common.command.GaiaCommand;
import me.moros.gaia.common.locale.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.minecraft.extras.RichDescription;

public record VersionCommand(Commander commander) implements GaiaCommand {
  @Override
  public void register() {
    commander().register(commander().rootBuilder()
      .literal("version")
      .commandDescription(RichDescription.of(Message.VERSION_CMD_DESC.build()))
      .permission(CommandPermissions.VERSION)
      .handler(c -> onVersion(c.sender()))
    );
  }

  private void onVersion(GaiaUser user) {
    String link = "https://github.com/PrimordialMoros/Gaia";
    Component version = Message.brand(Component.text("Version: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(user.parent().pluginInfo().version(), NamedTextColor.GREEN))
      .hoverEvent(HoverEvent.showText(Message.VERSION_COMMAND_HOVER.build(user.parent().pluginInfo().author(), link)))
      .clickEvent(ClickEvent.openUrl(link));
    user.sendMessage(version);
  }
}
