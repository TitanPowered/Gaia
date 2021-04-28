/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *
 * 	  This file is part of Gaia.
 *
 *    Gaia is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Gaia is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Gaia.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.gaia.commands;

import java.util.Comparator;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.HelpEntry;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import me.moros.gaia.GaiaPlugin;
import me.moros.gaia.api.Arena;
import me.moros.gaia.locale.Message;
import me.moros.gaia.platform.GaiaPlayer;
import me.moros.gaia.platform.GaiaUser;
import me.moros.gaia.util.Util;
import me.moros.gaia.util.functional.GaiaConsumerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

@CommandAlias("%gaiacommand")
public class GaiaCommand extends BaseCommand {
  private static final int AMOUNT_PER_PAGE = 12;

  @Dependency
  private static GaiaPlugin plugin;

  @HelpCommand
  @CommandPermission("gaia.command.help")
  @Description("View a list of all Gaia commands")
  public static void doHelp(GaiaUser user, CommandHelp help) {
    Message.HELP_HEADER.send(user);
    help.getHelpEntries().stream()
      .sorted(Comparator.comparing(HelpEntry::getCommand))
      .map(GaiaCommand::getEntryDetails).forEach(user::sendMessage);
  }

  @Subcommand("version|ver|v")
  @CommandPermission("gaia.command.version")
  @Description("View version info about Gaia")
  public static void onVersion(GaiaUser user) {
    String link = "https://github.com/PrimordialMoros/Gaia";
    Component version = Message.brand(Component.text("Version: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(plugin.getVersion(), NamedTextColor.GREEN))
      .hoverEvent(HoverEvent.showText(Message.VERSION_COMMAND_HOVER.build(plugin.getAuthor(), link)))
      .clickEvent(ClickEvent.openUrl(link));
    user.sendMessage(version);
  }

  @Subcommand("list|ls|l")
  @CommandPermission("gaia.command.list")
  @Description("List all Gaia arenas")
  public static void onList(GaiaUser user, @Default("1") int page) {
    int count = plugin.getArenaManager().getArenaCount();
    if (count == 0) {
      Message.LIST_NOT_FOUND.send(user);
      return;
    }
    int totalPages = (int) Math.ceil(count / (double) AMOUNT_PER_PAGE);
    if (page < 1 || page > totalPages) {
      Message.LIST_INVALID_PAGE.send(user);
      return;
    }
    int skip = (page - 1) * AMOUNT_PER_PAGE;
    TextComponent.Builder builder = Component.text().append(Component.text("Arenas - Page ", NamedTextColor.DARK_AQUA));
    if (page > 1) builder.append(generatePaging(false, page - 1));

    builder.append(Component.text(page, NamedTextColor.GREEN))
      .append(Component.text(" of ", NamedTextColor.DARK_AQUA))
      .append(Component.text(totalPages, NamedTextColor.GREEN));

    if (page < totalPages) builder.append(generatePaging(true, page + 1));
    user.sendMessage(builder.build());
    plugin.getArenaManager().getAllArenas().stream().sorted(Comparator.comparing(Arena::getName)).
      skip(skip).limit(AMOUNT_PER_PAGE).
      map(Arena::getInfo).forEach(user::sendMessage);
    user.sendMessage(Component.text(Util.generateLine(44), NamedTextColor.DARK_AQUA));
  }

  @Subcommand("info|i")
  @CommandPermission("gaia.command.info")
  @CommandCompletion("@arenas")
  @Description("View info about the specified arena or if no name is given, the arena you are currently in")
  public static void onInfo(GaiaUser user, @Optional @Flags("standing") Arena arena) {
    user.sendMessage(arena.getInfo());
  }

  @Subcommand("create|c|new|n")
  @CommandPermission("gaia.command.create")
  @Description("Create a new arena")
  public static void onCreate(GaiaPlayer user, String name) {
    String arenaName = Util.sanitizeInput(name);
    if (arenaName.length() < 3) {
      Message.CREATE_ERROR_VALIDATION.send(user);
      return;
    }
    if (plugin.getArenaManager().arenaExists(arenaName)) {
      Message.CREATE_ERROR_EXISTS.send(user, arenaName);
      return;
    }
    plugin.getArenaManager().createArena(user, arenaName);
  }

  @Subcommand("remove|rm|delete|del")
  @CommandPermission("gaia.command.remove")
  @CommandCompletion("@arenas")
  @Description("Remove an existing arena")
  public static void onRemove(GaiaUser user, Arena arena) {
    String arenaName = arena.getName();
    if (plugin.getArenaManager().removeArena(arenaName)) {
      Message.REMOVE_SUCCESS.send(user, arenaName);
    } else {
      Message.REMOVE_FAIL.send(user, arenaName);
    }
  }

  @Subcommand("revert|rev|reset|res|r")
  @CommandPermission("gaia.command.revert")
  @CommandCompletion("@arenas")
  @Description("Revert the specified arena")
  public static void onRevert(GaiaUser user, Arena arena) {
    if (!arena.isFinalized()) {
      Message.REVERT_ERROR_ANALYZING.send(user, arena.getFormattedName());
      return;
    }
    if (arena.isReverting()) {
      Message.REVERT_ERROR_REVERTING.send(user, arena.getFormattedName());
      return;
    }
    Message.REVERT_SUCCESS.send(user, arena.getFormattedName());
    GaiaConsumerInfo info = new GaiaConsumerInfo(user);
    plugin.getArenaManager().revertArena(arena, info);
  }

  @Subcommand("cancel|abort")
  @CommandPermission("gaia.command.cancel")
  @CommandCompletion("@arenas")
  @Description("Cancel the revert task of the specified arena or if no name is given, the arena you are currently in.")
  public static void onCancel(GaiaUser user, @Optional @Flags("standing") Arena arena) {
    if (arena.isReverting()) {
      plugin.getArenaManager().cancelRevertArena(arena);
    } else {
      Message.CANCEL_FAIL.send(user, arena.getFormattedName());
    }
  }

  private static Component generatePaging(boolean forward, int page) {
    return Component.text(forward ? " >>>" : "<<< ", NamedTextColor.GOLD)
      .hoverEvent(HoverEvent.showText(Component.text("Click to navigate to page " + page, NamedTextColor.GRAY)))
      .clickEvent(ClickEvent.runCommand("/gaia list " + page));
  }

  private static Component getEntryDetails(HelpEntry entry) {
    String command = entry.getCommand().toLowerCase();
    String name = command.substring(command.lastIndexOf(" ") + 1);
    String usage = "/" + command + " " + entry.getParameterSyntax();
    String permission = "gaia.command." + name;
    final Component details = Component.text()
      .append(Component.text("Command: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(Util.capitalize(name), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Description: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(entry.getDescription(), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Usage: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(usage, NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Permission: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(permission, NamedTextColor.GREEN)).append(Component.newline()).append(Component.newline())
      .append(Component.text("Click to auto-complete.", NamedTextColor.GRAY)).build();

    return Component.text("> ", NamedTextColor.DARK_GRAY).append(Component.text(usage, NamedTextColor.DARK_AQUA))
      .hoverEvent(HoverEvent.showText(details))
      .clickEvent(ClickEvent.suggestCommand(usage));
  }
}
