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

package com.github.primordialmoros.gaia.platform;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public interface GaiaSender {
	String getName();

	default boolean isPlayer() {
		return false;
	}

	boolean hasPermission(String permission);

	default void sendBrandedMessage(String text, TextColor color) {
		sendBrandedMessage(TextComponent.of(text, color));
	}

	default void sendBrandedMessage(String text) {
		sendBrandedMessage(TextComponent.of(text));
	}

	void sendBrandedMessage(TextComponent text);

	default void sendMessage(String text, TextColor color) {
		sendMessage(TextComponent.of(text, color));
	}

	default void sendMessage(String text) {
		sendMessage(TextComponent.of(text));
	}

	void sendMessage(TextComponent text);
}
