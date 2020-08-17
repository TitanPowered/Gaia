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

import com.github.primordialmoros.gaia.io.GaiaIO;
import com.github.primordialmoros.gaia.util.GaiaChunkRegion;
import com.github.primordialmoros.gaia.util.GaiaVector;

public class GaiaChunkMetadata {

	public GaiaVector min;
	public GaiaVector max;

	public String path;
	public String hash;

	public GaiaChunkMetadata(GaiaChunkRegion region, String path, String hash) {
		min = region.getRegion().getMinimumPoint();
		max = region.getRegion().getMaximumPoint();
		this.path = path;
		this.hash = hash;
	}


	public static boolean isValidMetadata(GaiaChunkMetadata m) {
		if (m.path == null  || m.hash == null || m.min == null || m.max == null) return false;
		if (!m.path.endsWith(GaiaIO.DATA_SUFFIX)) return false;
		return m.hash.length() == 32;
	}
}
