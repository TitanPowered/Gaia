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
import java.util.stream.Collectors;

import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.common.platform.VanillaLevel;
import net.kyori.adventure.key.Key;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class BukkitLevel extends VanillaLevel {
  private final World bukkitWorld;

  public BukkitLevel(World world) {
    super(((CraftWorld) world).getHandle());
    this.bukkitWorld = world;
  }

  @Override
  public @NonNull Key key() {
    return bukkitWorld.key();
  }

  @Override
  public void fixLight(Collection<ChunkPosition> positions) {
    ServerChunkCache chunkSource = handle().getChunkSource();
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
