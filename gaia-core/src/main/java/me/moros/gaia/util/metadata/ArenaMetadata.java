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

package me.moros.gaia.util.metadata;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.math.BlockVector3;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.ArenaPoint;
import me.moros.gaia.util.Util;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ArenaMetadata extends GaiaMetadata {
  public int version;

  public String name;
  public String world;

  public BlockVector3 min;
  public BlockVector3 max;

  public int amount;
  public List<ChunkMetadata> chunks;
  public List<ArenaPoint> points;

  public ArenaMetadata(@NonNull Arena arena) {
    version = VERSION;
    name = arena.name();
    world = arena.worldUID().toString();
    min = arena.region().min();
    max = arena.region().max();
    amount = arena.amount();
    chunks = new ArrayList<>(amount);
    points = arena.points();
  }

  @Override
  public boolean isValidMetadata() {
    if (version != VERSION) {
      return false;
    }
    if (name == null || world == null || min == null || max == null || chunks == null) {
      return false;
    }
    if (!Util.validateInput(name)) {
      return false;
    }
    if (!BlockVector3.isLongPackable(min) || !BlockVector3.isLongPackable(max)) {
      return false;
    }
    return amount > 0 && chunks.size() == amount;
  }

  public synchronized void addChunkMetadata(@NonNull ChunkMetadata metadata) {
    chunks.add(metadata);
  }
}
