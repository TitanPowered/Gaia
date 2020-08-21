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
import com.github.primordialmoros.gaia.platform.GaiaSender;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class VersionCommand extends GaiaCommand {

	public VersionCommand(final GaiaPlugin plugin) {
		super(plugin, "version", "/gaia version", "View version info about Gaia", new String[]{ "v", "ver" });
	}

	@Override
	public boolean execute(final GaiaSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 0)) return false;

		sender.sendBrandedMessage(TextComponent.builder("Version: ", NamedTextColor.DARK_AQUA).append(plugin.getVersion(), NamedTextColor.GREEN).build());
		sender.sendBrandedMessage(TextComponent.builder("Developed by: ", NamedTextColor.DARK_AQUA).append(plugin.getAuthor(), NamedTextColor.GREEN).build());
		return true;
	}
}
