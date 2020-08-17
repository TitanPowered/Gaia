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

import com.github.primordialmoros.gaia.Arena;
import com.github.primordialmoros.gaia.ArenaManager;
import com.github.primordialmoros.gaia.Gaia;
import com.github.primordialmoros.gaia.methods.CoreMethods;
import com.github.primordialmoros.gaia.util.GaiaVector;
import com.github.primordialmoros.gaia.util.Util;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class InfoCommand extends GaiaCommand {

	public InfoCommand() {
		super("info", "/gaia info [name]", "View info about the specified arena or if no name is given, the arena you are currently in.", new String[]{ "i" });
	}

	@Override
	public boolean execute(final CommandSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1)) return false;

		final Arena arena;
		if (args.size() == 0) {
			if (!isPlayer(sender)) return false;
			final Player player = (Player) sender;
			final GaiaVector loc = GaiaVector.at(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
			final Optional<Arena> result = ArenaManager.getArenaAtPoint(player.getWorld().getUID(), loc);
			if (result.isPresent()) {
				arena = result.get();
			} else {
				CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.YELLOW + "You are not currently standing in an arena.");
				return false;
			}
		} else {
			final String arenaName = Util.sanitizeInput(args.get(0));
			if (arenaName.length() < 3 || !ArenaManager.arenaExists(arenaName)) {
				CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.RED + "Could not find arena " + ChatColor.GOLD + arenaName);
				return false;
			}
			arena = ArenaManager.getArena(arenaName);
		}
		CoreMethods.sendMessage(sender, arena.getInfo());
		return true;
	}

	@Override
	public boolean completeArenaNames() {
		return true;
	}
}
