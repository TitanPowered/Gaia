/*
 * Copyright 2022 Moros
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

package me.moros.gaia.api;

import java.text.NumberFormat;

import com.sk89q.worldedit.math.Vector3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public record ArenaPoint(Vector3 v, float yaw, float pitch) {
  public Component details() {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(2);

    return Component.text()
      .append(Component.text("Coordinates: ", NamedTextColor.DARK_AQUA))
      .append(Component.newline())
      .append(Component.text(nf.format(v.getX()) + ", " + nf.format(v.getY()) + ", " + nf.format(v.getZ()), NamedTextColor.GREEN))
      .append(Component.newline())
      .append(Component.text("Direction", NamedTextColor.DARK_AQUA))
      .append(Component.newline())
      .append(Component.text(nf.format(yaw) + ", " + nf.format(pitch), NamedTextColor.GREEN))
      .append(Component.newline()).append(Component.newline())
      .append(Component.text("Click to teleport to point.", NamedTextColor.GRAY))
      .build();
  }
}
