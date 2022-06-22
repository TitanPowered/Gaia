package me.moros.gaia.api;

import java.text.NumberFormat;

import com.sk89q.worldedit.math.Vector3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public record ArenaPoint(Vector3 v, float yaw, float pitch) {
  public @NonNull Component details() {
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
