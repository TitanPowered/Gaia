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

import com.github.primordialmoros.gaia.Gaia;
import com.github.primordialmoros.gaia.methods.CoreMethods;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class VersionCommand extends GaiaCommand {

	public VersionCommand() {
		super("version", "/gaia version", "View version info about Gaia", new String[]{ "v", "ver" });
	}

	@Override
	public boolean execute(final CommandSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 0)) return false;

		CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.DARK_AQUA + "Version: " + ChatColor.GREEN + Gaia.getVersion());
		CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.DARK_AQUA + "Developed by: " + ChatColor.GREEN + Gaia.getAuthor());
		return true;
	}
}
