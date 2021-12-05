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

package me.moros.gaia.util.metadata;

import java.util.Objects;

import com.sk89q.worldedit.math.BlockVector3;
import me.moros.gaia.api.GaiaChunk;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChunkMetadata extends GaiaMetadata {
  public BlockVector3 min;
  public BlockVector3 max;

  public String id;
  public String hash;

  public ChunkMetadata(@NonNull GaiaChunk region, @NonNull String hash) {
    min = region.region().min();
    max = region.region().max();
    this.id = region.id().toString();
    this.hash = Objects.requireNonNull(hash);
  }

  @Override
  public boolean isValidMetadata() {
    if (id == null || hash == null || min == null || max == null) {
      return false;
    }
    if (id.isEmpty()) {
      return false;
    }
    return hash.length() == 32;
  }
}
