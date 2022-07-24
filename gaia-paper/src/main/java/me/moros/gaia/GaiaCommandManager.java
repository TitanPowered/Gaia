/*
 * Copyright 2020-2022 Moros
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

package me.moros.gaia;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Supplier;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.minecraft.extras.MinecraftHelp.HelpColors;
import cloud.commandframework.paper.PaperCommandManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import io.leangen.geantyref.TypeToken;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.command.GaiaCommand;
import me.moros.gaia.locale.Message;
import me.moros.gaia.util.Util;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class GaiaCommandManager extends PaperCommandManager<GaiaUser> {
  private final Gaia plugin;
  private final MinecraftHelp<GaiaUser> help;

  GaiaCommandManager(@NonNull Gaia plugin) throws Exception {
    super(plugin,
      CommandExecutionCoordinator.simpleCoordinator(),
      BukkitGaiaUser::new,
      u -> ((BukkitGaiaUser) u).sender()
    );
    this.plugin = plugin;
    registerExceptionHandler();
    registerAsynchronousCompletions();
    commandSuggestionProcessor(this::suggestionProvider);
    registerParsers();
    new GaiaCommand(plugin, this);
    this.help = MinecraftHelp.createNative("/gaia help", this);
    help.setMaxResultsPerPage(9);
    help.setHelpColors(HelpColors.of(
      NamedTextColor.DARK_GRAY,
      NamedTextColor.DARK_AQUA,
      NamedTextColor.GRAY,
      NamedTextColor.AQUA,
      NamedTextColor.GRAY)
    );
  }

  public @NonNull MinecraftHelp<GaiaUser> help() {
    return help;
  }

  private void registerExceptionHandler() {
    new MinecraftExceptionHandler<GaiaUser>()
      .withInvalidSyntaxHandler()
      .withInvalidSenderHandler()
      .withNoPermissionHandler()
      .withArgumentParsingHandler()
      .withCommandExecutionHandler()
      .withDecorator(Message::brand)
      .apply(this, AudienceProvider.nativeAudience());
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

  private void registerParsers() {
    parserRegistry().registerParserSupplier(TypeToken.get(Arena.class), options -> new ArenaParser());
    parserRegistry().registerParserSupplier(TypeToken.get(GaiaUser.class), options -> new GaiaUserParser());
  }

  private final class ArenaParser implements ArgumentParser<GaiaUser, Arena> {
    @Override
    public @NonNull ArgumentParseResult<Arena> parse(@NonNull CommandContext<@NonNull GaiaUser> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
      String input = inputQueue.peek();
      if (input == null) {
        return ArgumentParseResult.failure(new NoInputProvidedException(ArenaParser.class, commandContext));
      }
      inputQueue.remove();
      String sanitized = Util.sanitizeInput(input);
      Supplier<ArgumentParseResult<Arena>> failure = () -> ArgumentParseResult.failure(new Exception("Could not find the specified arena."));
      if (sanitized.equalsIgnoreCase("cur")) {
        if (commandContext.getSender().isPlayer()) {
          Player bukkitPlayer = (Player) ((BukkitGaiaUser) commandContext.getSender()).sender();
          UUID worldId = bukkitPlayer.getWorld().getUID();
          BlockVector3 point = BukkitAdapter.adapt(bukkitPlayer).getLocation().toVector().toBlockPoint();
          return plugin.arenaManager().stream()
            .filter(a -> a.worldUID().equals(worldId) && a.region().contains(point)).findAny()
            .map(ArgumentParseResult::success).orElseGet(failure);
        }
      } else {
        return plugin.arenaManager().arena(sanitized).map(ArgumentParseResult::success).orElseGet(failure);
      }
      return failure.get();
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(final @NonNull CommandContext<GaiaUser> commandContext, final @NonNull String input) {
      return plugin.arenaManager().sortedNames();
    }
  }

  private final class GaiaUserParser implements ArgumentParser<GaiaUser, GaiaUser> {
    @Override
    public @NonNull ArgumentParseResult<GaiaUser> parse(@NonNull CommandContext<@NonNull GaiaUser> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
      String input = inputQueue.peek();
      if (input == null) {
        return ArgumentParseResult.success(commandContext.getSender());
      }
      inputQueue.remove();
      String sanitized = Util.sanitizeInput(input);
      if (sanitized.equalsIgnoreCase("me")) {
        return ArgumentParseResult.success(commandContext.getSender());
      }
      Player player = plugin.getServer().getPlayer(sanitized);
      if (player == null) {
        return ArgumentParseResult.success(commandContext.getSender());
      }
      return ArgumentParseResult.success(new BukkitGaiaUser(player));
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(final @NonNull CommandContext<GaiaUser> commandContext, final @NonNull String input) {
      return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
    }
  }
}
