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

package com.github.primordialmoros.gaia.util.functional;

import org.bukkit.command.CommandSender;

public class GaiaConsumerInfo {

	public final CommandSender sender;
	public final String success, fail;
	public final long startTime;

	public GaiaConsumerInfo(final CommandSender sender, final String success, final String fail) {
		this.sender = sender;
		this.success = success;
		this.fail = fail;
		startTime = System.currentTimeMillis();
	}
}
