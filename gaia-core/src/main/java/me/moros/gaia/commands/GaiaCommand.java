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
import me.moros.gaia.platform.GaiaPlayer;
import me.moros.gaia.platform.GaiaSender;
import me.moros.gaia.util.Util;
import me.moros.gaia.util.functional.GaiaConsumerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Comparator;

@CommandAlias("%gaiacommand")
public class GaiaCommand extends BaseCommand {
	private static final int AMOUNT_PER_PAGE = 12;

	@Dependency
	private static GaiaPlugin plugin;

	@HelpCommand
	@CommandPermission("gaia.command.help")
	@Description("View a list of all Gaia commands")
	public static void doHelp(GaiaSender sender, CommandHelp help) {
		sender.sendBrandedMessage(Component.text("List of Gaia Commands:", NamedTextColor.YELLOW));
		help.getHelpEntries().stream()
			.sorted(Comparator.comparing(HelpEntry::getCommand))
			.map(GaiaCommand::getEntryDetails).forEach(sender::sendMessage);
	}

	@Subcommand("version|ver|v")
	@CommandPermission("gaia.command.version")
	@Description("View version info about Gaia")
	public static void onVersion(GaiaSender sender) {
		sender.sendBrandedMessage(getVersionInfo());
	}

	@Subcommand("list|ls|l")
	@CommandPermission("gaia.command.list")
	@Description("List all Gaia arenas")
	public static void onList(GaiaSender sender, @Default("1") int page) {
		int count = plugin.getArenaManager().getArenaCount();
		if (count == 0) {
			sender.sendBrandedMessage("No arenas found.", NamedTextColor.YELLOW);
			return;
		}
		int totalPages = (int) Math.ceil(count / (double) AMOUNT_PER_PAGE);
		if (page < 1 || page > totalPages) {
			sender.sendBrandedMessage(" Invalid page number!", NamedTextColor.RED);
			return;
		}
		int skip = (page - 1) * AMOUNT_PER_PAGE;
		Component text = Component.text("Arenas - Page ", NamedTextColor.DARK_AQUA);
		if (page > 1) text.append(generatePaging(false, page - 1));
		text.append(Component.text("{current} of {total}", NamedTextColor.DARK_AQUA)
				.replaceFirstText("{current}", Component.text(page, NamedTextColor.GREEN))
				.replaceFirstText("{total}", Component.text(totalPages, NamedTextColor.GREEN))
			);
		if (page < totalPages) text.append(generatePaging(true, page + 1));
		sender.sendBrandedMessage(text);
		plugin.getArenaManager().getAllArenas().stream().sorted(Comparator.comparing(Arena::getName)).
			skip(skip).limit(AMOUNT_PER_PAGE).
			map(Arena::getInfo).forEach(sender::sendMessage);
		sender.sendMessage(Util.generateLine(44), NamedTextColor.DARK_AQUA);
	}

	@Subcommand("info|i")
	@CommandPermission("gaia.command.info")
	@CommandCompletion("@arenas")
	@Description("View info about the specified arena or if no name is given, the arena you are currently in")
	public static void onInfo(GaiaSender sender, @Optional @Flags("standing") Arena arena) {
		sender.sendMessage(arena.getInfo());
	}

	@Subcommand("create|c|new|n")
	@CommandPermission("gaia.command.create")
	@Description("Create a new arena")
	public static void onCreate(GaiaPlayer sender, String name) {
		String arenaName = Util.sanitizeInput(name);
		if (arenaName.length() < 3) {
			sender.sendBrandedMessage("Arena names can only consist of alpha-numeric characters and must be between 3 and 32 characters long!", NamedTextColor.RED);
			return;
		}
		if (plugin.getArenaManager().arenaExists(arenaName)) {
			sender.sendBrandedMessage(Component.text(arenaName, NamedTextColor.GOLD).append(Component.text(" already exists! Choose a different name.", NamedTextColor.RED)));
			return;
		}
		plugin.getArenaManager().createArena(sender, arenaName);
	}

	@Subcommand("remove|rm|delete|del")
	@CommandPermission("gaia.command.remove")
	@CommandCompletion("@arenas")
	@Description("Remove an existing arena")
	public static void onRemove(GaiaSender sender, Arena arena) {
		String arenaName = arena.getName();
		if (plugin.getArenaManager().removeArena(arenaName)) {
			sender.sendBrandedMessage(Component.text(arenaName, NamedTextColor.GOLD)
				.append(Component.text(" has been deleted.", NamedTextColor.GREEN)));
		} else {
			sender.sendBrandedMessage(Component.text("Error, could not delete files for ", NamedTextColor.RED)
				.append(Component.text(arenaName, NamedTextColor.GOLD)));
		}
	}

	@Subcommand("revert|rev|reset|res|r")
	@CommandPermission("gaia.command.revert")
	@CommandCompletion("@arenas")
	@Description("Revert the specified arena")
	public static void onRevert(GaiaSender sender, Arena arena) {
		if (!arena.isFinalized()) {
			sender.sendBrandedMessage(arena.getFormattedName().append(Component.text(" is not fully analyzed yet!", NamedTextColor.YELLOW)));
			return;
		}
		if (arena.isReverting()) {
			sender.sendBrandedMessage(arena.getFormattedName().append(Component.text(" is currently being reverted!", NamedTextColor.YELLOW)));
			return;
		}
		sender.sendBrandedMessage(Component.text("Reverting ", NamedTextColor.GREEN).append(arena.getFormattedName()));
		GaiaConsumerInfo info = new GaiaConsumerInfo(sender);
		plugin.getArenaManager().revertArena(arena, info);
	}

	@Subcommand("cancel|abort")
	@CommandPermission("gaia.command.cancel")
	@CommandCompletion("@arenas")
	@Description("Cancel the revert task of the specified arena or if no name is given, the arena you are currently in.")
	public static void onCancel(GaiaSender sender, @Optional @Flags("standing") Arena arena) {
		if (arena.isReverting()) {
			plugin.getArenaManager().cancelRevertArena(arena);
		} else {
			sender.sendBrandedMessage(arena.getFormattedName()
				.append(Component.text(" is not currently being reverted!", NamedTextColor.YELLOW)));
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
		final Component details = Component.text("Command: {name}", NamedTextColor.DARK_AQUA).append(Component.newline())
			.append(Component.text("Description: {description}", NamedTextColor.DARK_AQUA)).append(Component.newline())
			.append(Component.text("Usage: {usage}", NamedTextColor.DARK_AQUA)).append(Component.newline())
			.append(Component.text("Permission: {permission}", NamedTextColor.DARK_AQUA)).append(Component.newline()).append(Component.newline())
			.append(Component.text("Click to auto-complete.", NamedTextColor.GRAY))
			.replaceFirstText("{name}", Component.text(Util.capitalize(name), NamedTextColor.GREEN))
			.replaceFirstText("{description}", Component.text(entry.getDescription(), NamedTextColor.GREEN))
			.replaceFirstText("{usage}", Component.text(usage, NamedTextColor.GREEN))
			.replaceFirstText("{permission}", Component.text(permission, NamedTextColor.GREEN));

		return Component.text("> ", NamedTextColor.DARK_GRAY).append(Component.text(usage, NamedTextColor.DARK_AQUA))
			.hoverEvent(HoverEvent.showText(details))
			.clickEvent(ClickEvent.suggestCommand(usage));
	}

	private static Component getVersionInfo() {
		String link = "https://github.com/PrimordialMoros/Gaia";
		Component details = Component.text("Developed by: {author}", NamedTextColor.DARK_AQUA).append(Component.newline())
			.append(Component.text("Source code: {link}", NamedTextColor.DARK_AQUA)).append(Component.newline())
			.append(Component.text("Licensed under: {license}", NamedTextColor.DARK_AQUA)).append(Component.newline()).append(Component.newline())
			.append(Component.text("Click to open link.", NamedTextColor.GRAY))
			.replaceFirstText("{author}", Component.text(plugin.getAuthor(), NamedTextColor.GREEN))
			.replaceFirstText("{link}", Component.text(link, NamedTextColor.GREEN))
			.replaceFirstText("{license}", Component.text("AGPLv3", NamedTextColor.GREEN));

		return Component.text("Version: {version}", NamedTextColor.DARK_AQUA)
			.replaceFirstText("{version}", Component.text(plugin.getVersion(), NamedTextColor.GREEN))
			.hoverEvent(HoverEvent.showText(details))
			.clickEvent(ClickEvent.openUrl(link));
	}
}
