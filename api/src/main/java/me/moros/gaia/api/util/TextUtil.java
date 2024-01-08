/*
 * Copyright 2020-2024 Moros
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

package me.moros.gaia.api.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TextUtil {
  private TextUtil() {
  }

  private static final Pattern NON_ALPHANUMERICAL = Pattern.compile("[^A-Za-z0-9]");
  private static final Pattern VALID = Pattern.compile("[a-z0-9]{3,64}");

  /**
   * Strip input of all non alphanumeric values and limit to 32 characters long
   * @param input the input string to sanitize
   * @return the sanitized output string
   */
  public static String sanitizeInput(@Nullable String input) {
    if (input == null) {
      return "";
    }
    String output = NON_ALPHANUMERICAL.matcher(input).replaceAll("").toLowerCase();
    return output.length() > 32 ? output.substring(0, 32) : output;
  }

  public static boolean validateInput(String input) {
    return VALID.matcher(input).matches();
  }

  public static String generateLine(int count) {
    char[] line = new char[count];
    Arrays.fill(line, '-');
    return new String(line);
  }

  public static String description(int size) {
    final int chunkArea = ChunkUtil.CHUNK_SIZE * ChunkUtil.CHUNK_SIZE;
    if (size <= 2 * 2 * chunkArea * 64) { // 65_536 blocks
      return "Tiny";
    } else if (size <= 5 * 5 * chunkArea * 128) { // 819_200 blocks
      return "Small";
    } else if (size <= 10 * 10 * chunkArea * 192) { // 4_915_200 blocks
      return "Medium";
    } else if (size <= 16 * 16 * chunkArea * 256) { // 16_777_216 blocks
      return "Large";
    } else {
      return "Huge";
    }
  }

  public static String formatDuration(@Positive long deltaTime) {
    Duration duration = Duration.ofMillis(deltaTime);
    List<String> parts = new ArrayList<>();
    long days = duration.toDaysPart();
    if (days > 0) {
      parts.add(days + "d");
    }
    int hours = duration.toHoursPart();
    if (hours > 0 || !parts.isEmpty()) {
      parts.add(hours + "h");
    }
    int minutes = duration.toMinutesPart();
    if (minutes > 0 || !parts.isEmpty()) {
      parts.add(minutes + "m");
    }
    int seconds = Math.max(1, duration.toSecondsPart());
    parts.add(seconds + "s");
    return String.join(" ", parts);
  }
}
