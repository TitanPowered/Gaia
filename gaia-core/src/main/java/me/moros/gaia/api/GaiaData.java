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

package me.moros.gaia.api;

import me.moros.gaia.platform.GaiaBlockData;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class GaiaData {
	private final GaiaBlockData[][][] data;
	private final GaiaVector size;

	public GaiaData(@NonNull GaiaVector size) {
		this.data = new GaiaBlockData[size.getX()][size.getY()][size.getZ()];
		this.size = size;
	}

	public @NonNull GaiaBlockData getDataAt(@NonNull GaiaVector v) {
		return getDataAt(v.getX(), v.getY(), v.getZ());
	}

	public @NonNull GaiaBlockData getDataAt(int x, int y, int z) {
		return data[x][y][z];
	}

	public void setDataAt(@NonNull GaiaVector v, @NonNull GaiaBlockData gaiaBlockData) {
		setDataAt(v.getX(), v.getY(), v.getZ(), gaiaBlockData);
	}

	public void setDataAt(int x, int y, int z, @NonNull GaiaBlockData gaiaBlockData) {
		data[x][y][z] = gaiaBlockData;
	}

	public @NonNull GaiaVector getVector() {
		return size;
	}
}
