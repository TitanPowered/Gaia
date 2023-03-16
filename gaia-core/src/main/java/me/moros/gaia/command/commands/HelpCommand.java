/*
 * Copyright 2023 Moros
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

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.StringArgument.StringMode;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.minecraft.extras.MinecraftHelp.HelpColors;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.command.CommandPermissions;
import me.moros.gaia.command.Commander;
import me.moros.gaia.command.GaiaCommand;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.format.NamedTextColor;

public record HelpCommand(Commander commander, MinecraftHelp<GaiaUser> help) implements GaiaCommand {
  public HelpCommand(Commander commander) {
    this(commander, createHelp(commander.manager()));
  }

  @Override
  public void register() {
    var builder = commander().rootBuilder();
    commander().register(builder
      .handler(c -> help().queryCommands(c.getOrDefault("query", ""), c.getSender()))
    );
    commander().register(builder.literal("help", "h")
      .meta(CommandMeta.DESCRIPTION, "View info about Gaia commands")
      .permission(CommandPermissions.HELP)
      .argument(StringArgument.optional("query", StringMode.GREEDY))
      .handler(c -> help().queryCommands(c.getOrDefault("query", ""), c.getSender()))
    );
  }

  private static <C extends Audience> MinecraftHelp<C> createHelp(CommandManager<C> manager) {
    var help = MinecraftHelp.createNative("/gaia help", manager);
    help.setMaxResultsPerPage(9);
    help.setHelpColors(HelpColors.of(
      NamedTextColor.DARK_GRAY,
      NamedTextColor.DARK_AQUA,
      NamedTextColor.GRAY,
      NamedTextColor.AQUA,
      NamedTextColor.GRAY)
    );
    return help;
  }
}
