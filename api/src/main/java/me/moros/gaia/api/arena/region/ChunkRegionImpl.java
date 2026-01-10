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

package me.moros.gaia.api.arena.region;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

record ChunkRegionImpl(int x, int z, Region region) implements ChunkRegion {
  record Validated(ChunkRegion chunk, long checksum, AtomicBoolean revert,
                   AtomicLong lastRevert) implements ChunkRegion.Validated {
    public Validated(ChunkRegion chunk, long checksum) {
      this(chunk, checksum, new AtomicBoolean(), new AtomicLong());
    }

    @Override
    public boolean reverting() {
      return revert().get();
    }

    @Override
    public void reverting(boolean value) {
      revert().set(value);
    }

    @Override
    public long lastReverted() {
      return lastRevert().get();
    }

    @Override
    public void resetLastReverted() {
      lastRevert().set(System.currentTimeMillis());
    }

    @Override
    public int x() {
      return chunk().x();
    }

    @Override
    public int z() {
      return chunk().z();
    }

    @Override
    public Region region() {
      return chunk().region();
    }

    @Override
    public String toString() {
      return chunk().toString();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj instanceof ChunkRegionImpl.Validated other) {
        return region().equals(other.region()) && checksum() == other.checksum();
      }
      return false;
    }

    @Override
    public int hashCode() {
      return 31 * region().hashCode() + Long.hashCode(checksum());
    }
  }
}
