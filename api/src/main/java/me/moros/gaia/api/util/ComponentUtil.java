/*
 * Copyright 2020-2023 Moros
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

import java.text.NumberFormat;
import java.util.Arrays;

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.Point;
import me.moros.math.Position;
import me.moros.math.Vector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class ComponentUtil {
  private ComponentUtil() {
  }

  private static final NumberFormat NUMBER_FORMAT;
  private static final String DELIMITER = ", ";

  static {
    NUMBER_FORMAT = NumberFormat.getInstance();
    NUMBER_FORMAT.setMinimumFractionDigits(0);
    NUMBER_FORMAT.setMaximumFractionDigits(2);
  }

  public static Component pointInfo(Point point) {
    return text().color(DARK_AQUA)
      .append(text("Position: ").append(text(formatPoint(point), GREEN)))
      .appendNewline()
      .append(text("Yaw/Pitch: ").append(text(formatDir(point.yaw(), point.pitch()), GREEN)))
      .appendNewline().appendNewline()
      .append(text("Click to teleport to point.", GRAY))
      .build();
  }

  public static Component generatePointInfo(Arena arena, Point point, int index) {
    return text()
      .append(text("[", DARK_GRAY)).append(text(index, DARK_AQUA)).append(text("]", DARK_GRAY))
      .hoverEvent(HoverEvent.showText(pointInfo(point)))
      .clickEvent(ClickEvent.runCommand("/gaia teleport " + arena.name() + " " + index))
      .build();
  }

  public static Component arenaInfoAsHover(Arena arena) {
    final Component infoDetails = arena.info().appendNewline().appendNewline()
      .append(text("Click to copy center coordinates to clipboard.", GRAY));
    return text()
      .append(text("> ", DARK_GRAY)).append(arena.displayName())
      .hoverEvent(HoverEvent.showText(infoDetails))
      .clickEvent(ClickEvent.copyToClipboard(formatVec(arena.region().center(), " ")))
      .build();
  }

  public static Component generateInfo(Arena arena) {
    int volume = arena.region().volume();
    return text().color(DARK_AQUA)
      .append(text("Name: "))
      .append(text(arena.name(), GREEN)).appendNewline()
      .append(text("World: "))
      .append(text(arena.level().value(), GREEN)).appendNewline()
      .append(text("Region: "))
      .append(text(arena.region().toString(), GREEN)).appendNewline()
      .append(text("Dimensions: "))
      .append(text(formatVec(arena.region().size(), " x "), GREEN)).appendNewline()
      .append(text("Volume: "))
      .append(text(String.format("%,d", volume) + " (" + TextUtil.description(volume) + ")", GREEN)).appendNewline()
      .append(text("Center: "))
      .append(text(Arrays.toString(arena.region().center().toArray()), GREEN))
      .build();
  }

  private static String formatPoint(Position vector) {
    return "[" + NUMBER_FORMAT.format(vector.x())
      + DELIMITER + NUMBER_FORMAT.format(vector.y())
      + DELIMITER + NUMBER_FORMAT.format(vector.z()) + "]";
  }

  private static String formatDir(float yaw, float pitch) {
    return NUMBER_FORMAT.format(yaw) + DELIMITER + NUMBER_FORMAT.format(pitch);
  }

  private static String formatVec(Vector3i vector, String delimiter) {
    return vector.blockX() + delimiter + vector.blockY() + delimiter + vector.blockZ();
  }
}
