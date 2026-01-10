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

package me.moros.gaia.common.command;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.common.command.commands.ArenaCommand;
import me.moros.gaia.common.command.commands.HelpCommand;
import me.moros.gaia.common.command.commands.PointCommand;
import me.moros.gaia.common.command.commands.VersionCommand;
import me.moros.gaia.common.locale.Message;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.slf4j.Logger;

record CommanderImpl(CommandManager<GaiaUser> manager, Gaia plugin, Logger logger) implements Commander {
  CommanderImpl init() {
    registerExceptionHandler();
    Collection<Function<Commander, GaiaCommand>> cmds = List.of(
      ArenaCommand::new, HelpCommand::new, PointCommand::new, VersionCommand::new
    );
    cmds.forEach(cmd -> cmd.apply(this).register());
    return this;
  }

  @Override
  public Command.Builder<GaiaUser> rootBuilder() {
    return manager().commandBuilder("gaia", RichDescription.of(Message.BASE_CMD_DESC.build()), "g");
  }

  @Override
  public void register(Command.Builder<GaiaUser> builder) {
    manager().command(builder);
  }

  private void registerExceptionHandler() {
    MinecraftExceptionHandler.<GaiaUser>createNative().defaultHandlers().decorator(Message::brand)
      .registerTo(manager());
  }
}
