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

package me.moros.gaia.command.argument;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import me.moros.gaia.api.GaiaUser;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class GaiaUserArgument extends CommandArgument<GaiaUser, GaiaUser> {
  private GaiaUserArgument(boolean required, String name, String defaultValue,
                           @Nullable BiFunction<CommandContext<GaiaUser>, String, List<String>> suggestionsProvider,
                           ArgumentDescription defaultDescription) {
    super(required, name, new Parser(), defaultValue, GaiaUser.class, suggestionsProvider, defaultDescription);
  }

  public static Builder builder(String name) {
    return new GaiaUserArgument.Builder(name);
  }

  public static GaiaUserArgument of(String name) {
    return GaiaUserArgument.builder(name).build();
  }

  public static GaiaUserArgument optional(String name) {
    return GaiaUserArgument.builder(name).asOptional().build();
  }

  public static GaiaUserArgument optional(String name, String defaultValue) {
    return GaiaUserArgument.builder(name).asOptionalWithDefault(defaultValue).build();
  }

  public static final class Builder extends TypedBuilder<GaiaUser, GaiaUser, Builder> {
    private Builder(String name) {
      super(GaiaUser.class, name);
    }

    @Override
    public GaiaUserArgument build() {
      return new GaiaUserArgument(
        this.isRequired(),
        this.getName(),
        this.getDefaultValue(),
        this.getSuggestionsProvider(),
        this.getDefaultDescription()
      );
    }
  }

  public static final class Parser implements ArgumentParser<GaiaUser, GaiaUser> {
    @Override
    public ArgumentParseResult<GaiaUser> parse(CommandContext<GaiaUser> commandContext, Queue<String> inputQueue) {
      String input = inputQueue.peek();
      if (input == null) {
        return ArgumentParseResult.success(commandContext.getSender());
      }
      inputQueue.remove();
      if (input.equalsIgnoreCase("me")) {
        return ArgumentParseResult.success(commandContext.getSender());
      }
      var user = commandContext.getSender().parent().findUser(input);
      if (user != null) {
        return ArgumentParseResult.success(user);
      } else {
        return ArgumentParseResult.failure(new Throwable("Could not find the specified user."));
      }
    }

    @Override
    public List<String> suggestions(CommandContext<GaiaUser> commandContext, String input) {
      return commandContext.getSender().parent().users().toList();
    }
  }
}


