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

package me.moros.gaia.platform;

import me.moros.gaia.api.GaiaVector;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class WorldWrapper implements GaiaWorld {
	private final World world;

	public WorldWrapper(@NonNull World world) {
		this.world = world;
	}

	public @NonNull World get() {
		return this.world;
	}

	@Override
	public @NonNull GaiaBlock getBlockAt(@NonNull GaiaVector v) {
		return new BlockWrapper(world.getBlockAt(v.getX(), v.getY(), v.getZ()));
	}

	@Override
	public @NonNull String getName() {
		return world.getName();
	}

	@Override
	public @NonNull UUID getUID() {
		return world.getUID();
	}
}
