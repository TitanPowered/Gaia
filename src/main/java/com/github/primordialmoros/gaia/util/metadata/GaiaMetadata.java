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

package com.github.primordialmoros.gaia.util.metadata;

import com.github.primordialmoros.gaia.Arena;
import com.github.primordialmoros.gaia.util.GaiaVector;
import com.github.primordialmoros.gaia.util.Util;

import java.util.List;

public class GaiaMetadata {

	public static final transient int VERSION = 1;

	public int version;

	public String name;
	public String world;

	public GaiaVector min;
	public GaiaVector max;

	public int amount;
	public List<GaiaChunkMetadata> chunks;

	public GaiaMetadata(Arena arena) {
		version = 1;
		name = arena.getName();
		world = arena.getWorldUID().toString();
		min = arena.getRegion().getMinimumPoint();
		max = arena.getRegion().getMaximumPoint();
		amount = arena.getSubRegions().size();
	}

	public static boolean isValidMetadata(GaiaMetadata m) {
		if (m.version != VERSION) return false;
		if (m.name == null || m.world == null || m.min == null || m.max == null || m.chunks == null) return false;
		if (!Util.validateInput(m.name)) return false;
		if (!GaiaVector.isValidVector(m.min) || !GaiaVector.isValidVector(m.max)) return false;
		return m.amount > 0 && m.chunks.size() == m.amount;
	}
}
