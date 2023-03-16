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

package me.moros.gaia.command;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import cloud.commandframework.Command.Builder;
import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import me.moros.gaia.GaiaPlugin;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.command.commands.ArenaCommand;
import me.moros.gaia.command.commands.HelpCommand;
import me.moros.gaia.command.commands.PointCommand;
import me.moros.gaia.command.commands.ReloadCommand;
import me.moros.gaia.command.commands.VersionCommand;
import me.moros.gaia.locale.Message;

record CommanderImpl(CommandManager<GaiaUser> manager, GaiaPlugin plugin) implements Commander {
  CommanderImpl init() {
    registerExceptionHandler();
    manager().commandSuggestionProcessor(this::suggestionProvider);
    Collection<Function<Commander, GaiaCommand>> cmds = List.of(
      ArenaCommand::new, HelpCommand::new, PointCommand::new, ReloadCommand::new, VersionCommand::new
    );
    cmds.forEach(cmd -> cmd.apply(this).register());
    return this;
  }

  @Override
  public Builder<GaiaUser> rootBuilder() {
    return manager().commandBuilder("gaia", "g", "arena", "arenas")
      .meta(CommandMeta.DESCRIPTION, "Base command for Gaia");
  }

  @Override
  public void register(Builder<GaiaUser> builder) {
    manager().command(builder);
  }

  private void registerExceptionHandler() {
    new MinecraftExceptionHandler<GaiaUser>().withDefaultHandlers().withDecorator(Message::brand)
      .apply(manager(), AudienceProvider.nativeAudience());
  }

  private List<String> suggestionProvider(CommandPreprocessingContext<GaiaUser> context, List<String> strings) {
    String input;
    if (context.getInputQueue().isEmpty()) {
      input = "";
    } else {
      input = context.getInputQueue().peek().toLowerCase(Locale.ROOT);
    }
    List<String> suggestions = new LinkedList<>();
    for (String suggestion : strings) {
      if (suggestion.toLowerCase(Locale.ROOT).startsWith(input)) {
        suggestions.add(suggestion);
      }
    }
    return suggestions;
  }
}
