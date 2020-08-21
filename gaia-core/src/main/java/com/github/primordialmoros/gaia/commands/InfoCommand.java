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
import com.github.primordialmoros.gaia.api.Arena;
import com.github.primordialmoros.gaia.api.GaiaVector;
import com.github.primordialmoros.gaia.platform.GaiaPlayer;
import com.github.primordialmoros.gaia.platform.GaiaSender;
import com.github.primordialmoros.gaia.util.Util;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Optional;

public class InfoCommand extends GaiaCommand {

	public InfoCommand(final GaiaPlugin plugin) {
		super(plugin, "info", "/gaia info [name]", "View info about the specified arena or if no name is given, the arena you are currently in.", new String[]{ "i" });
	}

	@Override
	public boolean execute(final GaiaSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1)) return false;

		final Arena arena;
		if (args.size() == 0) {
			if (!isPlayer(sender)) return false;
			final GaiaPlayer gaiaPlayer = (GaiaPlayer) sender;
			final GaiaVector loc = gaiaPlayer.getLocation();
			final Optional<Arena> result = plugin.getArenaManager().getArenaAtPoint(gaiaPlayer.getWorld().getUID(), loc);
			if (result.isPresent()) {
				arena = result.get();
			} else {
				sender.sendBrandedMessage("You are not currently standing in an arena.", NamedTextColor.YELLOW);
				return false;
			}
		} else {
			final String arenaName = Util.sanitizeInput(args.get(0));
			if (arenaName.length() < 3 || !plugin.getArenaManager().arenaExists(arenaName)) {
				sender.sendBrandedMessage(TextComponent.builder("Could not find arena ", NamedTextColor.RED).append(arenaName, NamedTextColor.GOLD).build());
				return false;
			}
			arena = plugin.getArenaManager().getArena(arenaName);
		}
		sender.sendMessage(arena.getInfo());
		return true;
	}

	@Override
	public boolean completeArenaNames() {
		return true;
	}
}
