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

package me.moros.gaia.nms.v1_18_R2;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import me.moros.gaia.LightFixer;
import me.moros.gaia.api.GaiaChunkPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;

public class LightFixerImpl implements LightFixer {
  @Override
  public void accept(UUID worldUuid, Collection<GaiaChunkPos> chunks) {
    if (Bukkit.getWorld(worldUuid) instanceof CraftWorld world) {
      ServerChunkCache chunkSource = world.getHandle().getChunkSource();
      Set<ChunkPos> chunkSet = chunks.stream().map(gc -> new ChunkPos(gc.x(), gc.z()))
        .filter(v -> filter(chunkSource, v)).collect(Collectors.toCollection(LinkedHashSet::new));
      chunkSource.getLightEngine().relight(chunkSet, c -> onChunkRelight(c.x, c.z), this::onComplete);
    }
  }

  private boolean filter(ServerChunkCache chunkSource, ChunkPos pos) {
    ChunkAccess chunk = (ChunkAccess) chunkSource.getChunkForLighting(pos.x, pos.z);
    return chunk != null && chunk.isLightCorrect() && chunk.getStatus().isOrAfter(ChunkStatus.LIGHT);
  }
}
