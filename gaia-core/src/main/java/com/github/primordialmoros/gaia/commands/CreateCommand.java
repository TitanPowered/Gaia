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

import com.github.primordialmoros.gaia.GaiaPlugin;
import com.github.primordialmoros.gaia.platform.GaiaPlayer;
import com.github.primordialmoros.gaia.platform.GaiaSender;
import com.github.primordialmoros.gaia.util.Util;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class CreateCommand extends GaiaCommand {

	public CreateCommand(final GaiaPlugin plugin) {
		super(plugin, "create", "/arena create <name>", "Create a new arena", new String[]{ "c", "new", "n" });
	}

	@Override
	public boolean execute(final GaiaSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 1) || !isPlayer(sender)) return false;

		final String arenaName = Util.sanitizeInput(args.get(0));
		if (arenaName.length() < 3) {
			sender.sendBrandedMessage("Arena names can only consist of alpha-numeric characters and must be between 3 and 32 characters long!", NamedTextColor.RED);
			return false;
		}
		if (plugin.getArenaManager().arenaExists(arenaName)) {
			sender.sendBrandedMessage(TextComponent.builder(arenaName, NamedTextColor.GOLD).append(" already exists! Choose a different name.", NamedTextColor.RED).build());
			return false;
		}
		return plugin.getArenaManager().createArena((GaiaPlayer) sender, arenaName);
	}
}
