/*
 * Copyright 2020-2024 Moros
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

package me.moros.gaia.common.command.parser;

import me.moros.gaia.api.platform.GaiaUser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public final class GaiaUserParser implements ArgumentParser<GaiaUser, GaiaUser>, BlockingSuggestionProvider.Strings<GaiaUser> {
  private GaiaUserParser() {
  }

  @Override
  public ArgumentParseResult<GaiaUser> parse(CommandContext<GaiaUser> commandContext, CommandInput commandInput) {
    String input = commandInput.peekString();
    GaiaUser user;
    if (input.equalsIgnoreCase("me")) {
      user = commandContext.sender();
    } else {
      user = commandContext.sender().parent().userService().findUser(input);
    }
    if (user != null) {
      commandInput.readString();
      return ArgumentParseResult.success(user);
    } else {
      return ArgumentParseResult.failure(new Throwable("Could not find the specified user"));
    }
  }

  @Override
  public Iterable<String> stringSuggestions(CommandContext<GaiaUser> commandContext, CommandInput commandInput) {
    return commandContext.sender().parent().userService().users().toList();
  }

  public static ParserDescriptor<GaiaUser, GaiaUser> parser() {
    return ParserDescriptor.of(new GaiaUserParser(), GaiaUser.class);
  }
}


