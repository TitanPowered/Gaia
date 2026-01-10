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

package me.moros.gaia.common.platform;

import java.util.concurrent.CompletableFuture;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.api.platform.Level;
import me.moros.gaia.api.util.ChunkUtil;
import me.moros.gaia.common.util.IndexedIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

@SuppressWarnings("resource")
public abstract class VanillaLevel implements Level {
  private static final TicketType GAIA_TICKET_TYPE = new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING);
  private static final int GAIA_TICKET_LEVEL = ChunkLevel.byStatus(ChunkStatus.FULL);

  private final ServerLevel handle;

  protected VanillaLevel(ServerLevel handle) {
    this.handle = handle;
  }

  protected ServerLevel handle() {
    return handle;
  }

  @Override
  public CompletableFuture<Boolean> restoreSnapshot(Snapshot snapshot, int amount) {
    if (amount > 0 && snapshot instanceof GaiaSnapshot gaiaSnapshot) {
      return loadChunkAsync(snapshot.x(), snapshot.z()).thenApply(c -> restoreSnapshotNow(gaiaSnapshot, amount));
    }
    return CompletableFuture.completedFuture(false);
  }

  private boolean restoreSnapshotNow(GaiaSnapshot snapshot, int amount) {
    var chunkSource = chunkSource();
    var levelChunk = chunkSource.getChunkNow(snapshot.x(), snapshot.z());
    if (levelChunk == null) {
      return false;
    }
    var offset = ChunkUtil.toChunkSectionPos(snapshot.chunk().region().min());
    final int xOffset = offset.blockX();
    final int yOffset = offset.blockY();
    final int zOffset = offset.blockZ();
    final IndexedIterator<BlockState> it = snapshot.iterator();
    final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    int counter = 0;
    BlockState toRestore;
    while (it.hasNext() && ++counter <= amount) {
      int index = it.index();
      toRestore = it.next();
      final int y = yOffset + (index / 256);
      final int z = zOffset + ((index % 256) / 16);
      final int x = xOffset + ((index % 256) % 16);
      if (snapshot.chunk().region().contains(x, y, z)) {
        BlockState result = levelChunk.setBlockState(mutablePos.set(x, y, z), toRestore, 512);
        if (result != null && result != toRestore) {
          chunkSource.blockChanged(mutablePos);
        }
      }
    }
    return it.hasNext();
  }

  @Override
  public CompletableFuture<Snapshot> snapshot(ChunkRegion chunk) {
    return loadChunkAsync(chunk.x(), chunk.z()).thenApply(c -> VanillaSnapshot.from(chunk, c));
  }

  @Override
  public CompletableFuture<?> loadChunkWithTicket(int x, int z) {
    return loadChunkAsync(x, z).thenAccept(c -> addChunkTicket(x, z));
  }

  protected CompletableFuture<ChunkAccess> loadChunkAsync(int x, int z) {
    return chunkSource().getChunkFuture(x, z, ChunkStatus.FEATURES, false)
      .thenApply(result -> result.orElseThrow(IllegalStateException::new));
  }

  @Override
  public void addChunkTicket(int x, int z) {
    var chunkPos = new ChunkPos(x, z);
    chunkSource().addTicket(gaiaTicket(), chunkPos);
  }

  protected Ticket gaiaTicket() {
    return new Ticket(GAIA_TICKET_TYPE, GAIA_TICKET_LEVEL);
  }

  protected ServerChunkCache chunkSource() {
    return handle().getChunkSource();
  }
}
