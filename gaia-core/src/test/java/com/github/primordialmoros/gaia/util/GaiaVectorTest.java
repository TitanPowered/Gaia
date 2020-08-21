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

import com.github.primordialmoros.gaia.api.GaiaVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GaiaVectorTest {

	@Test
	public void atXZClamped() {
		GaiaVector a = GaiaVector.at(64, 64, 64);
		GaiaVector b = GaiaVector.atXZClamped(64, 64, 64, 0, 128, 0, 128);
		GaiaVector c = GaiaVector.atXZClamped(128, 64, 128, 64, 64, 64, 64);
		assertEquals(b, a);
		assertEquals(c, a);
	}

	@Test
	public void containedWithin() {
		GaiaVector a = GaiaVector.ZERO;
		GaiaVector b = GaiaVector.at(31, 31, 31);
		GaiaVector c = GaiaVector.at(16, 16, 16);
		assertTrue(c.containedWithin(a, b));
		assertFalse(c.containedWithin(b, a));
	}

	@Test
	public void isValidVector() {
		GaiaVector a = GaiaVector.at(0, -1, 0);
		GaiaVector b = GaiaVector.at(0, 256, 0);
		assertTrue(GaiaVector.isValidVector(GaiaVector.ZERO));
		assertFalse(GaiaVector.isValidVector(a));
		assertFalse(GaiaVector.isValidVector(b));
	}

	@Test
	public void equals() {
		GaiaVector a = GaiaVector.at(1, 1, 1);
		GaiaVector b = GaiaVector.ONE;
		GaiaVector c = GaiaVector.at(6, 6, 6);
		assertEquals(a, a);
		assertEquals(a, b);
		assertEquals(b, a);
		assertNotEquals(a, c);
		assertNotEquals(null, a);
	}
}
