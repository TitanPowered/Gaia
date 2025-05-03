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

package me.moros.gaia.fabric.platform;

import java.util.Collection;

import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.common.platform.VanillaLevel;
import me.moros.gaia.fabric.mixin.accessor.ServerChunkCacheAccess;
import net.kyori.adventure.key.Key;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.ChunkPos;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class FabricLevel extends VanillaLevel {
  public FabricLevel(ServerLevel handle) {
    super(handle);
  }

  @Override
  public void removeChunkTicket(int x, int z) {
    var chunkPos = new ChunkPos(x, z);
    ((ServerChunkCacheAccess) chunkSource()).gaia$ticketStorage().removeTicket(new Ticket(gaiaTicketType(), 2), chunkPos);
  }

  @Override
  public void fixLight(Collection<ChunkPosition> positions) {
  }

  @Override
  public @NonNull Key key() {
    return handle().dimension().location();
  }
}
