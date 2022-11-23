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

package me.moros.gaia.api;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sk89q.worldedit.math.BlockVector3;
import me.moros.gaia.util.metadata.ChunkMetadata;
import me.moros.gaia.util.metadata.GaiaMetadata;
import me.moros.gaia.util.metadata.Metadatable;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;

/**
 * A chunk aligned GaiaRegion.
 */
public class GaiaChunk implements Metadatable, Iterable<BlockVector3> {
  public static final Comparator<GaiaChunk> ZX_ORDER = Comparator.comparingInt(GaiaChunk::chunkZ).thenComparingInt(GaiaChunk::chunkX);

  private final UUID id;

  private final Arena parent;
  private final GaiaRegion chunk;
  private final int chunkX, chunkZ;

  private ChunkMetadata meta;

  private final AtomicBoolean reverting;

  public GaiaChunk(UUID id, Arena parent, GaiaRegion region) {
    this.id = Objects.requireNonNull(id);
    this.parent = Objects.requireNonNull(parent);
    chunkX = region.min().getX() / 16;
    chunkZ = region.min().getZ() / 16;
    chunk = region;
    reverting = new AtomicBoolean(false);
    parent.addSubRegion(this);
  }

  public UUID id() {
    return id;
  }

  public Arena parent() {
    return parent;
  }

  public int chunkX() {
    return chunkX;
  }

  public int chunkZ() {
    return chunkZ;
  }

  public GaiaRegion region() {
    return chunk;
  }

  public boolean reverting() {
    return reverting.get();
  }

  public void startReverting() {
    reverting.set(true);
  }

  public void cancelReverting() {
    reverting.set(false);
  }

  public synchronized boolean analyzed() {
    return meta != null && meta.hash != null && !meta.hash.isEmpty();
  }

  @Override
  public Iterator<BlockVector3> iterator() {
    return new Iterator<>() {
      private final BlockVector3 max = chunk.size();
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
  public @MonotonicNonNull GaiaMetadata metadata() {
    return meta;
  }

  @Override
  public void metadata(@NotNull GaiaMetadata meta) {
    this.meta = (ChunkMetadata) meta;
  }
}
