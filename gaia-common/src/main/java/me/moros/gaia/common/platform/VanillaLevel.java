/*
 * Copyright 2020-2023 Moros
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

package me.moros.gaia.common.platform;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.api.platform.Level;
import me.moros.gaia.common.util.IndexedIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;

@SuppressWarnings("resource")
public abstract class VanillaLevel implements Level {
  private static final TicketType<Unit> GAIA_TICKET_TYPE = TicketType.create("gaia", (u1, u2) -> 0);

  private final ServerLevel handle;

  protected VanillaLevel(ServerLevel handle) {
    this.handle = handle;
  }

  protected ServerLevel handle() {
    return handle;
  }

  @Override
  public boolean restoreSnapshot(Snapshot snapshot, int amount) {
    var chunkSource = chunkSource();
    var levelChunk = chunkSource.getChunkNow(snapshot.x(), snapshot.z());
    if (levelChunk != null && amount > 0 && snapshot instanceof GaiaSnapshot gaiaSnapshot) {
      var offset = gaiaSnapshot.chunk().region().min();
      final int xOffset = offset.blockX();
      final int yOffset = offset.blockY();
      final int zOffset = offset.blockZ();
      final IndexedIterator<BlockState> it = gaiaSnapshot.iterator();
      final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
      int counter = 0;
      int index = 0;
      BlockState toRestore;
      while (it.hasNext() && ++counter <= amount) {
        final int y = index / 256;
        final int z = (index % 256) / 16;
        final int x = (index % 256) % 16;
        index = it.index();
        toRestore = it.next();
        BlockState result = levelChunk.setBlockState(mutablePos.set(xOffset + x, yOffset + y, zOffset + z), toRestore, false);
        if (result != null && result != toRestore) {
          chunkSource.blockChanged(mutablePos);
        }
      }
      return it.hasNext();
    }
    return false;
  }

  @Override
  public Snapshot snapshot(ChunkRegion chunk) {
    var levelChunk = Objects.requireNonNull(chunkSource().getChunkNow(chunk.x(), chunk.z()), "Chunk not loaded!");
    return VanillaSnapshot.from(chunk, levelChunk);
  }

  @Override
  public CompletableFuture<?> loadChunkWithTicket(int x, int z) {
    var chunkPos = new ChunkPos(x, z);
    return chunkSource().getChunkFuture(x, z, ChunkStatus.EMPTY, false)
      .thenAccept(c -> chunkSource().addRegionTicket(GAIA_TICKET_TYPE, chunkPos, 0, Unit.INSTANCE));
  }

  @Override
  public void removeChunkTicket(int x, int z) {
    var chunkPos = new ChunkPos(x, z);
    chunkSource().removeRegionTicket(GAIA_TICKET_TYPE, chunkPos, 2, Unit.INSTANCE);
  }

  private ServerChunkCache chunkSource() {
    return handle().getChunkSource();
  }
}
