/*
 * Copyright 2020-2026 Moros
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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.ImmutableMinecraftHelp;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.StringParser;

public record HelpCommand(Commander commander, MinecraftHelp<GaiaUser> help) implements GaiaCommand {
  public HelpCommand(Commander commander) {
    this(commander, createHelp(commander.manager()));
  }

  @Override
  public void register() {
    var builder = commander().rootBuilder();
    commander().register(builder
      .commandDescription(RichDescription.of(Message.BASE_CMD_DESC.build()))
      .handler(c -> help().queryCommands("", c.sender()))
    );
    commander().register(builder
      .literal("help")
      .optional("query", StringParser.greedyStringParser())
      .commandDescription(RichDescription.of(Message.HELP_CMD_DESC.build()))
      .permission(CommandPermissions.HELP)
      .handler(c -> help.queryCommands(c.getOrDefault("query", ""), c.sender()))
    );
  }


  private static <C extends Audience> MinecraftHelp<C> createHelp(CommandManager<C> manager) {
    return ImmutableMinecraftHelp.copyOf(MinecraftHelp.createNative("/gaia help", manager))
      .withMaxResultsPerPage(9)
      .withColors(MinecraftHelp.helpColors(
        NamedTextColor.DARK_GRAY,
        NamedTextColor.DARK_AQUA,
        NamedTextColor.GRAY,
        NamedTextColor.AQUA,
        NamedTextColor.GRAY)
      );
  }
}
