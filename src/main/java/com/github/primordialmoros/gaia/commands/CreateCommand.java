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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateCommand extends GaiaCommand {

    public CreateCommand() {
        super("create", "/arena create <name>", "Create a new arena", new String[]{"c", "new", "n"});
    }

    @Override
    public boolean execute(final CommandSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 1) || !isPlayer(sender)) return false;

		final String arenaName = Util.sanitizeInput(args.get(0));
		if (arenaName.length() < 3) {
			CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.RED + "Arena names can only consist of alpha-numeric characters and must be between 3 and 32 characters long!");
			return false;
		}
		if (ArenaManager.arenaExists(arenaName)) {
			CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.GOLD + arenaName + ChatColor.RED + " already exists! Choose a different name.");
			return false;
		}
		final Player player = (Player) sender;
		return ArenaManager.createArena(player, arenaName, player.getWorld());
    }
}
