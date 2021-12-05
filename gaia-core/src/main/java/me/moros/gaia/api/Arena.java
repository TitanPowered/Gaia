/*
 * Copyright 2020-2021 Moros
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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

public class Arena implements Metadatable, Iterable<GaiaChunk> {
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

  public int amount() {
    return subRegions.size();
  }

  public @NonNull String name() {
    return name;
  }

  public @NonNull Component displayName() {
    return Component.text(name(), NamedTextColor.GOLD);
  }

  public @NonNull World world() {
    return world;
  }

  public @NonNull UUID worldUID() {
    return worldId;
  }

  public @NonNull GaiaRegion region() {
    return region;
  }

  public @NonNull Component info() {
    return info;
  }

  public boolean reverting() {
    return reverting;
  }

  public void reverting(boolean value) {
    reverting = value;
  }

  public boolean finalized() {
    return finalized;
  }

  private static Component createInfo(Arena arena) {
    final int volume = arena.region().volume();
    BlockVector3 c = arena.region().center();
    final Component infoDetails = Component.text()
      .append(Component.text("Name: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(arena.name(), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("World: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(arena.world().getName(), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Dimensions: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(arena.dimensions(), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Volume: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(String.valueOf(volume), NamedTextColor.GREEN)).append(Component.newline())
      .append(Component.text("Center: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(c.getX() + ", " + c.getY() + ", " + c.getZ(), NamedTextColor.GREEN))
      .append(Component.newline()).append(Component.newline())
      .append(Component.text("Click to copy center coordinates to clipboard.", NamedTextColor.GRAY))
      .build();
    return Component.text()
      .append(Component.text("> ", NamedTextColor.DARK_GRAY).append(arena.displayName()))
      .append(Component.text(" (" + Util.description(volume) + ")", NamedTextColor.DARK_AQUA))
      .hoverEvent(HoverEvent.showText(infoDetails))
      .clickEvent(ClickEvent.copyToClipboard(c.getX() + " " + c.getY() + " " + c.getZ()))
      .build();
  }

  public @NonNull String dimensions() {
    return region.size().getX() + " x " + region.size().getY() + " x " + region.size().getZ();
  }

  public long lastReverted() {
    return lastReverted;
  }

  public void resetLastReverted() {
    lastReverted = System.currentTimeMillis();
  }

  @Override
  public @MonotonicNonNull GaiaMetadata metadata() {
    return meta;
  }

  @Override
  public void metadata(@NonNull GaiaMetadata meta) {
    this.meta = (ArenaMetadata) meta;
  }

  public @NonNull Stream<GaiaChunk> stream() {
    return subRegions.stream();
  }

  @Override
  public @NonNull Iterator<GaiaChunk> iterator() {
    return Collections.unmodifiableList(subRegions).iterator();
  }
}
