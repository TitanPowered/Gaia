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
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class RemoveCommand extends GaiaCommand {
	public RemoveCommand(final GaiaPlugin plugin) {
		super(plugin, "remove", "/gaia remove <name>", "Remove an existing arena", new String[]{ "rm", "delete", "del" });
	}

	@Override
	public boolean execute(final GaiaSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 1)) return false;

		final String arenaName = Util.sanitizeInput(args.get(0));
		if (arenaName.length() < 3 || !plugin.getArenaManager().arenaExists(arenaName)) {
			sender.sendBrandedMessage(TextComponent.builder("Could not find arena ", NamedTextColor.RED).append(arenaName, NamedTextColor.GOLD).build());
			return false;
		}
		if (plugin.getArenaManager().removeArena(arenaName)) {
			sender.sendBrandedMessage(TextComponent.builder(arenaName, NamedTextColor.GOLD).append(" has been deleted.", NamedTextColor.GREEN).build());
			return true;
		} else {
			sender.sendBrandedMessage(TextComponent.builder("Error, could not delete files for ", NamedTextColor.RED).append(arenaName, NamedTextColor.GOLD).build());
			return false;
		}
	}

	@Override
	public boolean completeArenaNames() {
		return true;
	}
}
