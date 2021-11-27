/*
 *   Copyright 2020-2021 Moros <https://github.com/PrimordialMoros>
 *
 *    This file is part of Gaia.
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import me.moros.gaia.util.Util;
import me.moros.gaia.util.metadata.ArenaMetadata;
import me.moros.gaia.util.metadata.GaiaMetadata;
import me.moros.gaia.util.metadata.Metadatable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Arena implements Metadatable {
  private final String name;
  private final World world;
  private final UUID worldId;
  private final GaiaRegion region;
  private final Component info;

  private final List<GaiaChunk> subRegions;

  private boolean reverting;
  private boolean finalized;

  private ArenaMetadata meta;

  private long lastReverted = 0;

  public Arena(@NonNull String name, @NonNull World world, @NonNull UUID worldId, @NonNull GaiaRegion region) {
    this.name = name.toLowerCase();
    this.world = world;
    this.worldId = worldId;
    this.region = region;
    info = createInfo(this);
    subRegions = new ArrayList<>();
    reverting = false;
    finalized = false;
  }

  public void addSubRegion(@NonNull GaiaChunk chunk) {
    subRegions.add(chunk);
  }

  public boolean finalizeArena() {
    if (subRegions.isEmpty() || finalized) {
      return false;
    }
    subRegions.sort(GaiaChunk.ZX_ORDER);
    finalized = true;
    return true;
  }

  public @NonNull List<@NonNull GaiaChunk> getSubRegions() {
    return subRegions;
  }

  public @NonNull String getName() {
    return name;
  }

  public @NonNull Component getFormattedName() {
    return Component.text(getName(), NamedTextColor.GOLD);
  }

  public @NonNull World getWorld() {
    return world;
  }

  public @NonNull UUID getWorldUID() {
    return worldId;
  }

  public @NonNull GaiaRegion getRegion() {
    return region;
  }

  public @NonNull Component getInfo() {
    return info;
  }

  public boolean isReverting() {
    return reverting;
  }

  public boolean isFinalized() {
    return finalized;
  }

  public void setReverting(boolean value) {
    reverting = value;
  }

  public static @NonNull Component createInfo(@NonNull Arena arena) {
    final int volume = arena.getRegion().getVolume();
    BlockVector3 c = arena.getRegion().getCenter();
    final Component infoDetails = Component.text()
      .append(Component.text("Name: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(arena.getName(), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("World: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(arena.getWorld().getName(), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Dimensions: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(arena.getDimensions(), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Volume: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(String.valueOf(volume), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Center: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(c.getX() + ", " + c.getY() + ", " + c.getZ(), NamedTextColor.GREEN))
      .append(Component.newline()).append(Component.newline())
      .append(Component.text("Click to copy center coordinates to clipboard.", NamedTextColor.GRAY))
      .build();
    return Component.text()
      .append(Component.text("> ", NamedTextColor.DARK_GRAY).append(arena.getFormattedName()))
      .append(Component.text(" (" + Util.getSizeDescription(volume) + ")", NamedTextColor.DARK_AQUA))
      .hoverEvent(HoverEvent.showText(infoDetails))
      .clickEvent(ClickEvent.copyToClipboard(c.getX() + " " + c.getY() + " " + c.getZ()))
      .build();
  }

  public @NonNull String getDimensions() {
    return region.getWidth() + " x " + region.getHeight() + " x " + region.getLength();
  }

  public long lastReverted() {
    return lastReverted;
  }

  public void resetLastReverted() {
    lastReverted = System.currentTimeMillis();
  }

  @Override
  public @MonotonicNonNull GaiaMetadata getMetadata() {
    return meta;
  }

  @Override
  public void setMetadata(@NonNull GaiaMetadata meta) {
    this.meta = (ArenaMetadata) meta;
  }
}
