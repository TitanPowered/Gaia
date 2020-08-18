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
import com.github.primordialmoros.gaia.util.Util;
import com.github.primordialmoros.gaia.util.functional.GaiaConsumerInfo;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RevertCommand extends GaiaCommand {

	public RevertCommand() {
		super("revert", "/arena revert <name>", "Reset the specified arena", new String[]{ "rev", "reset", "res", "r" });
	}

	@Override
	public boolean execute(final CommandSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 1)) return false;

		final String arenaName = Util.sanitizeInput(args.get(0));
		if (arenaName.length() < 3 || !ArenaManager.arenaExists(arenaName)) {
			CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.RED + "Could not find arena " + ChatColor.GOLD + arenaName);
			return false;
		}
		final Arena arena = ArenaManager.getArena(arenaName);
		if (!arena.isFinalized()) {
			CoreMethods.sendMessage(sender, Gaia.PREFIX + arena.getFormattedName() + ChatColor.YELLOW + " is not fully analyzed yet!");
			return false;
		}
		if (arena.isReverting()) {
			CoreMethods.sendMessage(sender, Gaia.PREFIX + arena.getFormattedName() + ChatColor.YELLOW + " is currently being reverted!");
			return false;
		}
		CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.GREEN + "Reverting " + arena.getFormattedName());
		final GaiaConsumerInfo info = new GaiaConsumerInfo(sender,
			Gaia.PREFIX + ChatColor.GREEN + "Finished reverting " + arena.getFormattedName(),
			Gaia.PREFIX + ChatColor.GREEN + "Cancelled reverting " + arena.getFormattedName()
		);
		ArenaManager.revertArena(arena, info);
		return true;
	}

	@Override
	public boolean completeArenaNames() {
		return true;
	}
}
