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

import com.github.primordialmoros.gaia.api.Arena;
import com.github.primordialmoros.gaia.api.GaiaVector;
import com.github.primordialmoros.gaia.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ArenaMetadata extends GaiaMetadata {
	public int version;

	public String name;
	public String world;

	public GaiaVector min;
	public GaiaVector max;

	public int amount;
	public List<ChunkMetadata> chunks;

	public ArenaMetadata(Arena arena) {
		version = VERSION;
		name = arena.getName();
		world = arena.getWorldUID().toString();
		min = arena.getRegion().getMinimumPoint();
		max = arena.getRegion().getMaximumPoint();
		amount = arena.getSubRegions().size();
		chunks = new ArrayList<>(amount);
	}

	@Override
	public boolean isValidMetadata() {
		if (version != VERSION) return false;
		if (name == null || world == null || min == null || max == null || chunks == null) return false;
		if (!Util.validateInput(name)) return false;
		if (!GaiaVector.isValidVector(min) || !GaiaVector.isValidVector(max)) return false;
		return amount > 0 && chunks.size() == amount;
	}
}
