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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.util.NumberConversions;

import java.util.Comparator;
import java.util.List;

public class ListCommand extends GaiaCommand {
	private static final int AMOUNT_PER_PAGE = 12;

    public ListCommand() {
        super("list", "/gaia list <page>", "Show a list of all arenas", new String[]{"l", "ls"});
    }

    @Override
    public boolean execute(final CommandSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1)) return false;

		int count = ArenaManager.getArenaCount();
		if (count == 0) {
			CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.YELLOW + "No arenas found.");
			return true;
		}
		int totalPages = NumberConversions.ceil(count / (double) AMOUNT_PER_PAGE);
		int currentPage = 1;
		if (args.size() > 0) {
			try {
				currentPage = Integer.parseInt(args.get(0));
			} catch (NumberFormatException e) {
				currentPage = 0;
			}
		}
		if (currentPage < 1 || currentPage > totalPages) {
			CoreMethods.sendMessage(sender, Gaia.PREFIX + ChatColor.RED + " Invalid page number!");
			return false;
		}
		int skip = (currentPage - 1) * AMOUNT_PER_PAGE;
		final ComponentBuilder cb = new ComponentBuilder(new TextComponent(Gaia.PREFIX + ChatColor.DARK_AQUA + "Arenas - Page "));
		if (currentPage > 1) cb.append(Util.generatePaging(false, currentPage - 1));
		cb.append(new TextComponent(ChatColor.GREEN + "" + currentPage + ChatColor.DARK_AQUA + " of " + ChatColor.GREEN + totalPages));
		if (currentPage < totalPages) cb.append(Util.generatePaging(true, currentPage + 1));
		CoreMethods.sendMessage(sender, cb.create());
		ArenaManager.getAllArenas().stream().sorted(Comparator.comparing(Arena::getName)).skip(skip).limit(AMOUNT_PER_PAGE).map(Arena::getInfo).forEach(info -> CoreMethods.sendMessage(sender, info));
		CoreMethods.sendMessage(sender, ChatColor.DARK_AQUA + Util.generateLine(44));
		return true;
    }
}
