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

import me.moros.gaia.GaiaPlugin;
import me.moros.gaia.platform.GaiaSender;
import me.moros.gaia.util.Util;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public abstract class GaiaCommand {
	public static final String NO_PERMISSION = "You do not have permission to execute this command";
	public static final String PLAYER_ONLY = "You must be a player to execute this command";

	public static final Map<String, GaiaCommand> commands = new HashMap<>();
	public static final Set<String> gaiaAliases = Set.of("g", "gaia", "arena", "arenas");

	protected final GaiaPlugin plugin;
	private final String name;
	private final String usage;
	private final String description;
	private final Set<String> aliases = new HashSet<>();
	private final TextComponent text;

	public abstract boolean execute(final GaiaSender sender, final List<String> args);

	public GaiaCommand(final GaiaPlugin plugin, final String name, final String usage, final String description, final String[] aliases) {
		this.plugin = plugin;
		this.name = name;
		this.usage = usage;
		this.description = description;
		this.aliases.add(name);
		this.aliases.addAll(Arrays.asList(aliases));

		final TextComponent details = TextComponent.builder("Command: ", NamedTextColor.DARK_AQUA)
			.append(Util.capitalize(getName()), NamedTextColor.GREEN).append(TextComponent.newline())
			.append("Description: ", NamedTextColor.DARK_AQUA)
			.append(getDescription(), NamedTextColor.GREEN).append(TextComponent.newline())
			.append("Usage: ", NamedTextColor.DARK_AQUA)
			.append(getUsage(), NamedTextColor.GREEN).append(TextComponent.newline())
			.append("Permission: ", NamedTextColor.DARK_AQUA)
			.append(getPermission(), NamedTextColor.GREEN).append(TextComponent.newline()).append(TextComponent.newline())
			.append("Click to auto-complete.", NamedTextColor.GRAY).build();

		text = TextComponent.builder("> ", NamedTextColor.DARK_GRAY).append(getUsage(), NamedTextColor.DARK_AQUA)
			.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, details))
			.clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, getUsage()))
			.build();

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

	protected boolean hasPermission(final GaiaSender sender) {
		if (sender.hasPermission(getPermission())) {
			return true;
		} else {
			sender.sendMessage(NO_PERMISSION, NamedTextColor.RED);
			return false;
		}
	}

	protected boolean correctLength(final GaiaSender sender, final int size, final int min, final int max) {
		if (size >= min && size <= max) {
			return true;
		} else {
			sender.sendMessage(TextComponent.builder("Proper Usage ", NamedTextColor.YELLOW).append(getTextComponent()).build());
			return false;
		}
	}

	protected boolean isPlayer(final GaiaSender sender) {
		if (sender.isPlayer()) {
			return true;
		} else {
			sender.sendMessage(PLAYER_ONLY, NamedTextColor.RED);
			return false;
		}
	}

	public static Stream<String> getFlatStream() {
		return commands.values().stream().map(GaiaCommand::getAliases).flatMap(Collection::stream);
	}

	public static void viewHelp(GaiaSender sender) {
		sender.sendBrandedMessage(TextComponent.of("List of Gaia Commands:", NamedTextColor.YELLOW));
		commands.values().stream().sorted(Comparator.comparing(GaiaCommand::getName)).
			filter(c -> sender.hasPermission(c.getPermission())).
			forEach(c -> sender.sendMessage(c.getTextComponent()));
	}
}
