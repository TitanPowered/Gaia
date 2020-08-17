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

/**
 * An axis-aligned immutable cuboid. It can be defined using a world and two corners of the cuboid.
 */
public final class GaiaRegion {

	private final GaiaVector minPoint, maxPoint, diff;

	public GaiaRegion(final GaiaVector pos1, final GaiaVector pos2) {
		if (!GaiaVector.isValidVector(pos1))
			throw new IllegalArgumentException("Location exceeds coordinate limits: " + pos1);
		if (!GaiaVector.isValidVector(pos2))
			throw new IllegalArgumentException("Location exceeds coordinate limits: " + pos2);

		minPoint = pos1.getMinimum(pos2);
		maxPoint = pos2.getMaximum(pos2);
		diff = maxPoint.subtract(minPoint).add(1, 1, 1);
	}

	public GaiaVector getMinimumPoint() {
		return minPoint;
	}

	public GaiaVector getMaximumPoint() {
		return maxPoint;
	}

	public GaiaVector getVector() {
		return diff;
	}

	public int getWidth() {
		return diff.getX();
	}

	public int getHeight() {
		return diff.getY();
	}

	public int getLength() {
		return diff.getZ();
	}

	public int getVolume() {
		return diff.getLength();
	}

	public GaiaVector getCenter() {
		return getMinimumPoint().add(getMaximumPoint()).divide(2);
	}

	public boolean contains(GaiaVector vector) {
		return vector.containedWithin(getMinimumPoint(), getMaximumPoint());
	}

	public boolean intersects(GaiaRegion region) {
		return (getMinimumPoint().getX() <= region.getMaximumPoint().getX() && getMaximumPoint().getX() >= region.getMinimumPoint().getX()) &&
			(getMinimumPoint().getY() <= region.getMaximumPoint().getY() && getMaximumPoint().getY() >= region.getMinimumPoint().getY()) &&
			(getMinimumPoint().getZ() <= region.getMaximumPoint().getZ() && getMaximumPoint().getZ() >= region.getMinimumPoint().getZ());
	}
}
