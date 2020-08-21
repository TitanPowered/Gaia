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

import com.github.primordialmoros.gaia.api.GaiaChunk;
import com.github.primordialmoros.gaia.api.GaiaVector;

public class ChunkMetadata extends GaiaMetadata {
	public GaiaVector min;
	public GaiaVector max;

	public String id;
	public String hash;

	public ChunkMetadata(GaiaChunk region, String hash) {
		min = region.getRegion().getMinimumPoint();
		max = region.getRegion().getMaximumPoint();
		this.id = region.getId().toString();
		this.hash = hash;
	}

	@Override
	public boolean isValidMetadata() {
		if (id == null || hash == null || min == null || max == null) return false;
		if (id.isEmpty()) return false;
		return hash.length() == 32;
	}
}
