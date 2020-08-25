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
import me.moros.gaia.api.Arena;
import me.moros.gaia.platform.GaiaSender;
import me.moros.gaia.util.Util;
import me.moros.gaia.util.functional.GaiaConsumerInfo;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class RevertCommand extends GaiaCommand {
	public RevertCommand(final GaiaPlugin plugin) {
		super(plugin, "revert", "/arena revert <name>", "Reset the specified arena", new String[]{ "rev", "reset", "res", "r" });
	}

	@Override
	public boolean execute(final GaiaSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 1)) return false;

		final String arenaName = Util.sanitizeInput(args.get(0));
		if (arenaName.length() < 3 || !plugin.getArenaManager().arenaExists(arenaName)) {
			sender.sendBrandedMessage(TextComponent.builder("Could not find arena ", NamedTextColor.RED).append(arenaName, NamedTextColor.GOLD).build());
			return false;
		}
		final Arena arena = plugin.getArenaManager().getArena(arenaName);
		if (!arena.isFinalized()) {
			sender.sendBrandedMessage(arena.getFormattedName().append(TextComponent.of(" is not fully analyzed yet!", NamedTextColor.YELLOW)));
			return false;
		}
		if (arena.isReverting()) {
			sender.sendBrandedMessage(arena.getFormattedName().append(TextComponent.of(" is currently being reverted!", NamedTextColor.YELLOW)));
			return false;
		}
		sender.sendBrandedMessage(TextComponent.builder("Reverting ", NamedTextColor.GREEN).append(arena.getFormattedName()).build());
		final GaiaConsumerInfo info = new GaiaConsumerInfo(sender);
		plugin.getArenaManager().revertArena(arena, info);
		return true;
	}

	@Override
	public boolean completeArenaNames() {
		return true;
	}
}
