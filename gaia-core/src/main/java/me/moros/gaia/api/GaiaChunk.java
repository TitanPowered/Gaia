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

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import com.sk89q.worldedit.math.BlockVector3;
import me.moros.gaia.util.metadata.ChunkMetadata;
import me.moros.gaia.util.metadata.GaiaMetadata;
import me.moros.gaia.util.metadata.Metadatable;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * A chunk aligned GaiaRegion.
 */
public class GaiaChunk implements Metadatable {
  public static final Comparator<GaiaChunk> ZX_ORDER = Comparator.comparingInt(GaiaChunk::getZ).thenComparingInt(GaiaChunk::getX);

  private final UUID id;

  private final Arena parent;
  private final GaiaRegion chunk;
  private final int chunkX, chunkZ;

  private ChunkMetadata meta;

  private boolean reverting;

  public GaiaChunk(@NonNull UUID id, @NonNull Arena parent, @NonNull GaiaRegion region) {
    this.id = Objects.requireNonNull(id);
    this.parent = Objects.requireNonNull(parent);
    chunkX = region.getMinimumPoint().getX() / 16;
    chunkZ = region.getMinimumPoint().getZ() / 16;
    chunk = region;
    reverting = false;
    parent.addSubRegion(this);
  }

  public @NonNull UUID getId() {
    return id;
  }

  public @NonNull Arena getParent() {
    return parent;
  }

  public int getX() {
    return chunkX;
  }

  public int getZ() {
    return chunkZ;
  }

  public @NonNull GaiaRegion getRegion() {
    return chunk;
  }

  public boolean isReverting() {
    return reverting;
  }

  public void startReverting() {
    reverting = true;
  }

  public void cancelReverting() {
    reverting = false;
  }

  public synchronized boolean isAnalyzed() {
    return meta != null && meta.hash != null && !meta.hash.isEmpty();
  }

  public @NonNull Iterator<BlockVector3> iterator() {
    return new Iterator<>() {
      private final BlockVector3 max = chunk.getVector();
      private int nextX = 0;
      private int nextY = 0;
      private int nextZ = 0;

      @Override
      public boolean hasNext() {
        return (nextX != Integer.MIN_VALUE);
      }

      @Override
      public BlockVector3 next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        BlockVector3 answer = BlockVector3.at(nextX, nextY, nextZ);
        if (++nextX >= max.getX()) {
          nextX = 0;
          if (++nextZ >= max.getZ()) {
            nextZ = 0;
            if (++nextY >= max.getY()) {
              nextX = Integer.MIN_VALUE;
            }
          }
        }
        return answer;
      }
    };
  }

  @Override
  public @MonotonicNonNull GaiaMetadata getMetadata() {
    return meta;
  }

  @Override
  public void setMetadata(@NotNull GaiaMetadata meta) {
    this.meta = (ChunkMetadata) meta;
  }
}
