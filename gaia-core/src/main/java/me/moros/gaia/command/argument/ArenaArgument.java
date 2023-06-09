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
import java.util.function.Supplier;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.sk89q.worldedit.math.BlockVector3;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.util.Util;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ArenaArgument extends CommandArgument<GaiaUser, Arena> {
  private ArenaArgument(boolean required, String name, String defaultValue,
                        @Nullable BiFunction<CommandContext<GaiaUser>, String, List<String>> suggestionsProvider,
                        ArgumentDescription defaultDescription) {
    super(required, name, new Parser(), defaultValue, Arena.class, suggestionsProvider, defaultDescription);
  }

  public static Builder builder(String name) {
    return new ArenaArgument.Builder(name);
  }

  public static ArenaArgument of(String name) {
    return ArenaArgument.builder(name).build();
  }

  public static ArenaArgument optional(String name) {
    return ArenaArgument.builder(name).asOptional().build();
  }

  public static ArenaArgument optional(String name, String defaultValue) {
    return ArenaArgument.builder(name).asOptionalWithDefault(defaultValue).build();
  }

  public static final class Builder extends TypedBuilder<GaiaUser, Arena, Builder> {
    private Builder(String name) {
      super(Arena.class, name);
    }

    @Override
    public ArenaArgument build() {
      return new ArenaArgument(
        this.isRequired(),
        this.getName(),
        this.getDefaultValue(),
        this.getSuggestionsProvider(),
        this.getDefaultDescription()
      );
    }
  }

  public static final class Parser implements ArgumentParser<GaiaUser, Arena> {
    @Override
    public ArgumentParseResult<Arena> parse(CommandContext<GaiaUser> commandContext, Queue<String> inputQueue) {
      String input = inputQueue.peek();
      if (input == null) {
        return ArgumentParseResult.failure(new NoInputProvidedException(Parser.class, commandContext));
      }
      inputQueue.remove();
      String sanitized = Util.sanitizeInput(input);
      Supplier<ArgumentParseResult<Arena>> failure = () -> ArgumentParseResult.failure(new Throwable("Could not find the specified arena."));
      if (sanitized.equalsIgnoreCase("cur")) {
        var user = commandContext.getSender();
        var worldUUID = user.worldKey();
        var arenaPoint = user.parent().pointFromUser(user);
        if (worldUUID != null && arenaPoint != null) {
          BlockVector3 point = arenaPoint.v().toBlockPoint();
          return user.parent().arenaManager().stream()
            .filter(a -> a.worldKey().equals(worldUUID) && a.region().contains(point)).findAny()
            .map(ArgumentParseResult::success).orElseGet(failure);
        }
      } else {
        return commandContext.getSender().parent().arenaManager().arena(sanitized)
          .map(ArgumentParseResult::success).orElseGet(failure);
      }
      return failure.get();
    }

    @Override
    public List<String> suggestions(CommandContext<GaiaUser> commandContext, String input) {
      return commandContext.getSender().parent().arenaManager().sortedNames();
    }
  }
}


