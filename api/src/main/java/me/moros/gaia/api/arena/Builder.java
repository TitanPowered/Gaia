/*
 * Copyright 2020-2026 Moros
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.api.util.TextUtil;
import net.kyori.adventure.key.Key;

public final class Builder {
  private String name;
  private Key level;
  private Region region;
  private List<ChunkRegion.Validated> chunkRegions;
  private List<Point> points;

  Builder() {
  }

  public Builder name(String name) {

    this.name = Objects.requireNonNull(name);
    return this;
  }

  public Builder level(Key level) {
    this.level = Objects.requireNonNull(level);
    return this;
  }

  public Builder region(Region region) {
    this.region = Objects.requireNonNull(region);
    return this;
  }

  public Builder chunks(Collection<ChunkRegion.Validated> chunkRegions) {
    this.chunkRegions = new ArrayList<>(chunkRegions);
    return this;
  }

  public Builder points(List<Point> points) {
    this.points = Objects.requireNonNull(points);
    return this;
  }

  private void validate() {
    Objects.requireNonNull(name);
    Objects.requireNonNull(level);
    Objects.requireNonNull(region);
    if (!TextUtil.validateInput(name)) {
      throw new IllegalArgumentException(String.format("Arena name %s contains illegal characters!", name));
    }
    for (var chunk : chunkRegions) {
      if (!region.contains(chunk.region().min()) || !region.contains(chunk.region().max())) {
        throw new IllegalArgumentException("Arena region does not contain chunk!");
      }
      // TODO ensure it covers the whole region?
    }
  }

  public Arena build() {
    validate();
    chunkRegions.sort(ChunkPosition.ZX_ORDER);
    var arena = new ArenaImpl(name, level, region, chunkRegions);
    if (points != null) {
      arena.addPoints(points);
    }
    return arena;
  }
}
