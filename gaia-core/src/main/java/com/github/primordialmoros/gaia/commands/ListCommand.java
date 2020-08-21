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
import com.github.primordialmoros.gaia.platform.GaiaSender;
import com.github.primordialmoros.gaia.util.Util;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Comparator;
import java.util.List;

public class ListCommand extends GaiaCommand {
	private static final int AMOUNT_PER_PAGE = 12;

	public ListCommand(final GaiaPlugin plugin) {
		super(plugin, "list", "/gaia list <page>", "Show a list of all arenas", new String[]{ "l", "ls" });
	}

	@Override
	public boolean execute(final GaiaSender sender, final List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1)) return false;

		int count = plugin.getArenaManager().getArenaCount();
		if (count == 0) {
			sender.sendBrandedMessage("No arenas found.", NamedTextColor.YELLOW);
			return true;
		}
		int totalPages = (int) Math.ceil(count / (double) AMOUNT_PER_PAGE);
		int currentPage = 1;
		if (!args.isEmpty()) {
			try {
				currentPage = Integer.parseInt(args.get(0));
			} catch (NumberFormatException e) {
				currentPage = 0;
			}
		}
		if (currentPage < 1 || currentPage > totalPages) {
			sender.sendBrandedMessage(" Invalid page number!", NamedTextColor.RED);
			return false;
		}
		int skip = (currentPage - 1) * AMOUNT_PER_PAGE;
		final TextComponent.Builder builder = TextComponent.builder("Arenas - Page ", NamedTextColor.DARK_AQUA);
		if (currentPage > 1) builder.append(generatePaging(false, currentPage - 1));
		builder.append(String.valueOf(currentPage), NamedTextColor.GREEN)
			.append(" of ", NamedTextColor.DARK_AQUA)
			.append(String.valueOf(totalPages), NamedTextColor.GREEN);
		if (currentPage < totalPages) builder.append(generatePaging(true, currentPage + 1));
		sender.sendBrandedMessage(builder.build());
		plugin.getArenaManager().getAllArenas().stream().sorted(Comparator.comparing(Arena::getName)).
			skip(skip).limit(AMOUNT_PER_PAGE).
			map(Arena::getInfo).forEach(sender::sendMessage);
		sender.sendMessage(Util.generateLine(44), NamedTextColor.DARK_AQUA);
		return true;
	}

	public static TextComponent generatePaging(boolean forward, int page) {
		return TextComponent.builder(forward ? " >>>" : "<<< ", NamedTextColor.GOLD)
			.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to navigate to page " + page, NamedTextColor.GRAY)))
			.clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/gaia list " + page)).build();
	}
}
