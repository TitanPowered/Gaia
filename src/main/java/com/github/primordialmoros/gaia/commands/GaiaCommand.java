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

package com.github.primordialmoros.gaia.commands;

import com.github.primordialmoros.gaia.ArenaManager;
import com.github.primordialmoros.gaia.Gaia;
import com.github.primordialmoros.gaia.methods.CoreMethods;
import com.github.primordialmoros.gaia.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class GaiaCommand {

	private static final String NO_PERMISSION = ChatColor.RED + "You do not have permission to execute this command";
	private static final String PLAYER_ONLY = ChatColor.RED + "You must be a player to execute this command";

	private static final Map<String, GaiaCommand> commands = new HashMap<>();
	private static final Set<String> gaiaAliases = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("g", "gaia", "arena", "arenas")));

	private final String name;
	private final String usage;
	private final String description;
	private final Set<String> aliases = new HashSet<>();
	private final TextComponent text;

	public abstract boolean execute(final CommandSender sender, final List<String> args);

	public GaiaCommand(final String name, final String usage, final String description, final String[] aliases) {
		this.name = name;
		this.usage = usage;
		this.description = description;
		this.aliases.add(name);
		this.aliases.addAll(Arrays.asList(aliases));

		text = new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.DARK_AQUA + getUsage());
		final String details = ChatColor.DARK_AQUA + "Command: " + ChatColor.GREEN + Util.capitalize(getName()) + "\n" +
			ChatColor.DARK_AQUA + "Description: " + ChatColor.GREEN + getDescription() + "\n" +
			ChatColor.DARK_AQUA + "Usage: " + ChatColor.GREEN + getUsage() + "\n" +
			ChatColor.DARK_AQUA + "Permission: " + ChatColor.GREEN + getPermission() + "\n\n" +
			ChatColor.GRAY + "Click to auto-complete.";
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(details)));
		text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getUsage()));

		commands.put(name, this);
	}

	public String getName() {
		return this.name;
	}

	public String getUsage() {
		return usage;
	}

	public String getDescription() {
		return description;
	}

	public Set<String> getAliases() {
		return aliases;
	}

	public String getPermission() {
		return "gaia.command." + getName();
	}

	public boolean completeArenaNames() {
		return false;
	}

	private TextComponent getTextComponent() {
		return text;
	}

	protected boolean hasPermission(final CommandSender sender) {
		return hasPermission(sender, "");
	}

	protected boolean hasPermission(final CommandSender sender, final String extra) {
		if (sender.hasPermission(getPermission() + (extra.isEmpty() ? "" : "." + extra))) {
			return true;
		} else {
			CoreMethods.sendMessage(sender, NO_PERMISSION);
			return false;
		}
	}

	protected boolean correctLength(final CommandSender sender, final int size, final int min, final int max) {
		if (size >= min && size <= max) {
			return true;
		} else {
			CoreMethods.sendMessage(sender, ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + usage);
			return false;
		}
	}

	protected boolean isPlayer(final CommandSender sender) {
		if (sender instanceof Player) {
			return true;
		} else {
			CoreMethods.sendMessage(sender, PLAYER_ONLY);
			return false;
		}
	}

	public static void registerCommands() {
		new ListCommand();
		new InfoCommand();
		new VersionCommand();
		new CreateCommand();
		new RemoveCommand();
		new RevertCommand();
		new CancelCommand();

		final PluginCommand baseCommand = Gaia.getPlugin().getCommand("gaia");
		if (baseCommand == null) return;

		final CommandExecutor executor = (sender, command, alias, args) -> {
			if (gaiaAliases.contains(alias.toLowerCase())) {
				if (args.length > 0) {
					final String arg0 = args[0].toLowerCase();
					for (final GaiaCommand gc : commands.values()) {
						if (gc.getAliases().contains(arg0)) {
							return gc.execute(sender, Arrays.asList(args).subList(1, args.length));
						}
					}
				}
				viewHelp(sender);
				return true;
			}
			return false;
		};

		final TabCompleter completer = (sender, command, alias, args) -> {
			final String argument = args.length > 0 ? args[0].toLowerCase() : "";
			if (args.length <= 1) {
				if (argument.isEmpty()) return new ArrayList<>(commands.keySet());
				return commands.values().stream().map(GaiaCommand::getAliases).flatMap(Collection::stream).filter(s -> s.startsWith(argument)).collect(Collectors.toList());
			} else if (args.length == 2) {
				for (final GaiaCommand gc : commands.values()) {
					if (gc.getAliases().contains(argument) && gc.completeArenaNames()) {
						return ArenaManager.getSortedArenaNames();
					}
				}
			}
			return new ArrayList<>();
		};

		baseCommand.setExecutor(executor);
		baseCommand.setTabCompleter(completer);
	}

	public static void viewHelp(CommandSender sender) {
		CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.YELLOW + " List of Gaia Commands:");
		commands.values().stream().sorted(Comparator.comparing(GaiaCommand::getName)).
			filter(c -> sender.hasPermission(c.getPermission())).
			forEach(c -> CoreMethods.sendMessage(sender, c.getTextComponent()));
	}
}
