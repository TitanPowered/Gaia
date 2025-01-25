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

package me.moros.gaia.api.arena;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.gaia.api.util.ComponentUtil;
import me.moros.gaia.api.util.supplier.Suppliers;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

final class ArenaImpl implements Arena {
  private final String name;
  private final Key level;
  private final Region region;
  private final Supplier<Component> infoSupplier;
  private final List<ChunkRegion.Validated> chunkRegions;
  private final List<Point> points;
  private final AtomicLong lastRevert;

  ArenaImpl(String name, Key level, Region region, Collection<ChunkRegion.Validated> chunkRegions) {
    this.name = name;
    this.level = level;
    this.region = region;
    this.infoSupplier = Suppliers.lazy(() -> ComponentUtil.generateInfo(this));
    this.chunkRegions = List.copyOf(chunkRegions);
    this.points = new CopyOnWriteArrayList<>();
    this.lastRevert = new AtomicLong();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Key level() {
    return level;
  }

  @Override
  public Region region() {
    return region;
  }

  @Override
  public Component info() {
    return infoSupplier.get();
  }

  @Override
  public long lastReverted() {
    return lastRevert.get();
  }

  @Override
  public void resetLastReverted() {
    lastRevert.set(System.currentTimeMillis());
  }

  @Override
  public void addPoint(Point point) {
    Objects.requireNonNull(point);
    if (region.contains(point)) {
      points.add(point);
    }
  }

  @Override
  public void clearPoints() {
    points.clear();
  }

  @Override
  public List<Point> points() {
    return List.copyOf(points);
  }

  @Override
  public Stream<Point> streamPoints() {
    return points.stream();
  }

  @Override
  public List<ChunkRegion.Validated> chunks() {
    return chunkRegions;
  }

  @Override
  public Stream<ChunkRegion.Validated> streamChunks() {
    return chunkRegions.stream();
  }

  @Override
  public Iterator<ChunkRegion.Validated> iterator() {
    return chunkRegions.iterator();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Arena other) {
      return level().equals(other.level()) && region().equals(other.region());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 * level().hashCode() + region().hashCode();
  }
}
