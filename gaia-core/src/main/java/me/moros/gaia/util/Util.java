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

package me.moros.gaia.util;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Util {
	private static final Pattern NON_ALPHANUMERICAL = Pattern.compile("[^A-Za-z0-9]");
	private static final Pattern VALID = Pattern.compile("[a-z0-9]{3,64}");

	/**
	 * Strip input of all non alpha-numeric values and limit to 32 characters long
	 * @param input the input string to sanitize
	 * @return the sanitized output string
	 */
	public static @NonNull String sanitizeInput(@NonNull String input) {
		String output = NON_ALPHANUMERICAL.matcher(input).replaceAll("").toLowerCase();
		return output.length() > 32 ? output.substring(0, 32) : output;
	}

	public static boolean validateInput(@NonNull String input) {
		return VALID.matcher(input).matches();
	}

	public static @NonNull String generateLine(int count) {
		char[] line = new char[count];
		Arrays.fill(line, '-');
		return new String(line);
	}

	public static @NonNull String capitalize(@NonNull String input) {
		if (input.length() < 2) return input.toUpperCase();
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	public static @NonNull String getSizeDescription(int size) {
		if (size <= 32768) {
			return "Tiny";
		} else if (size <= 262144) {
			return "Small";
		} else if (size <= 2097152) {
			return "Medium";
		} else if (size <= 8388608) {
			return "Large";
		} else {
			return "Huge";
		}
	}

	public static @NonNull String toHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
