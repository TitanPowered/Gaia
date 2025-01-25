/*
 * Copyright 2020-2025 Moros
 *
 * This file is part of Gaia.
 *
 * Gaia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gaia is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gaia. If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.gaia.common.storage;

import java.io.IOException;
import java.util.Locale;

public class ChecksumMismatchException extends IOException {
  private static final String MSG = "Checksum mismatch for file: %s, expected '%s' but found '%s'. File data might be corrupted.";

  public ChecksumMismatchException(String file, long expected, long provided) {
    super(formatMsg(file, expected, provided));
  }

  private static String formatMsg(String file, long expected, long provided) {
    return String.format(MSG, file, toHex(expected), toHex(provided));
  }

  private static String toHex(long checksum) {
    return Long.toHexString(checksum).toUpperCase(Locale.ROOT);
  }
}
