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

package com.github.primordialmoros.gaia.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.Arrays;

public class Util {

	/**
	 * Strip input of all non alpha-numeric values and limit to 32 characters long
	 * @param input the input string to sanitize
	 * @return the sanitized output string
	 */
	public static String sanitizeInput(final String input) {
		final String output = input.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
		return output.length() > 32 ? output.substring(0, 32) : output;
	}

	public static boolean validateInput(final String input) {
		return input.matches("[a-z0-9]{3,64}");
	}

	public static String generateLine(int count) {
		char[] line = new char[count];
		Arrays.fill(line, '-');
		return new String(line);
	}

	public static TextComponent generatePaging(boolean forward, int page) {
		final TextComponent paging = new TextComponent(forward ? " >>>" : "<<< ");
		paging.setColor(ChatColor.GOLD);
		paging.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "Click to navigate to page " + page)));
		paging.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gaia list " + page));
		return paging;
	}

    public static String capitalize(final String input) {
		if (input.length() < 2) return input.toUpperCase();
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	public static String getSizeDescription(int size) {
		if (size <= 32768) {
			return "Tiny";
		} else if (size <= 262144) {
			return "Small";
		} else if (size <= 2097152) {
			return "Medium";
		} else if (size <= 8388608) {
			return "Large";
		} else {
			return "Huge";
		}
	}

	public static String toHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
