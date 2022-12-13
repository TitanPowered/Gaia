/*
 * Copyright 2020-2022 Moros
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

package me.moros.gaia.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import me.moros.gaia.api.GaiaChunkPos;
import me.moros.gaia.api.GaiaRegion;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Util {
  private static final Pattern NON_ALPHANUMERICAL = Pattern.compile("[^A-Za-z0-9]");
  private static final Pattern VALID = Pattern.compile("[a-z0-9]{3,64}");

  /**
   * Strip input of all non alpha-numeric values and limit to 32 characters long
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

  public static String capitalize(String input) {
    if (input.length() < 2) {
      return input.toUpperCase();
    }
    return input.substring(0, 1).toUpperCase() + input.substring(1);
  }

  public static String description(int size) {
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

  public static String toHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public static List<GaiaChunkPos> spiralChunks(GaiaRegion region) {
    int sizeX = (region.max().getX() >> 4) - (region.min().getX() >> 4);
    int sizeZ = (region.max().getZ() >> 4) - (region.min().getZ() >> 4);

    int centerX = region.center().getX() >> 4;
    int centerZ = region.center().getZ() >> 4;

    int halfX = sizeX / 2;
    int halfZ = sizeZ / 2;

    int x = 0, z = 0, dx = 0, dz = -1;
    int t = Math.max(sizeX, sizeZ);
    int maxI = t * t;
    List<GaiaChunkPos> result = new ArrayList<>();
    for (int i = 0; i < maxI; i++) {
      if ((-halfX <= x) && (x <= halfX) && (-halfZ <= z) && (z <= halfZ)) {
        result.add(GaiaChunkPos.at(centerX + x, centerZ + z));
      }
      if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
        t = dx;
        dx = -dz;
        dz = t;
      }
      x += dx;
      z += dz;
    }
    return result;
  }
}
