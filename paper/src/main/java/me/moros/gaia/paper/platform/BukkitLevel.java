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

package me.moros.gaia.paper.platform;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.common.platform.VanillaLevel;
import net.kyori.adventure.key.Key;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class BukkitLevel extends VanillaLevel {
  private final World bukkitWorld;
  private final RegionExecutor executor;

  public BukkitLevel(World world, RegionExecutor executor) {
    super(((CraftWorld) world).getHandle());
    this.bukkitWorld = world;
    this.executor = executor;
  }

  @Override
  public @NonNull Key key() {
    return bukkitWorld.key();
  }

  @Override
  protected CompletableFuture<ChunkAccess> loadChunkAsync(int x, int z) {
    if (Folia.FOLIA) {
      return bukkitWorld.getChunkAtAsync(x, z, false).thenApply(c -> chunkSource().getChunkNow(x, z));
    } else {
      return super.loadChunkAsync(x, z);
    }
  }

  @Override
  public void fixLight(Collection<ChunkPosition> positions) {
    if (Folia.FOLIA) { // TODO thread context for folia?
      return;
    }
    executor.execute(() -> fixLightNow(positions));
  }

  private void fixLightNow(Collection<ChunkPosition> positions) {
    ServerChunkCache chunkSource = chunkSource();
    Set<ChunkPos> chunkSet = positions.stream().map(gc -> new ChunkPos(gc.x(), gc.z()))
      .filter(v -> filter(chunkSource, v)).collect(Collectors.toCollection(LinkedHashSet::new));
    chunkSource.getLightEngine().relight(chunkSet, c -> {
    }, i -> {
    });
  }

  private boolean filter(ServerChunkCache chunkSource, ChunkPos pos) {
    ChunkAccess chunk = (ChunkAccess) chunkSource.getChunkForLighting(pos.x, pos.z);
    return chunk != null && chunk.isLightCorrect() && chunk.getStatus().isOrAfter(ChunkStatus.LIGHT);
  }
}
