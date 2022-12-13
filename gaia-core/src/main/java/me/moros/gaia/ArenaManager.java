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

package me.moros.gaia;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sk89q.worldedit.math.BlockVector3;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaRegion;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.io.GaiaIO;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class ArenaManager implements Iterable<Arena> {
  private final Map<String, Arena> arenas = new ConcurrentHashMap<>();

  protected final GaiaPlugin plugin;

  protected ArenaManager(GaiaPlugin plugin) {
    this.plugin = plugin;
  }

  public Optional<Arena> arena(String name) {
    return Optional.ofNullable(arenas.get(name));
  }

  public boolean contains(String name) {
    return arenas.containsKey(name) || GaiaIO.instance().arenaFileExists(name);
  }

  public List<String> sortedNames() {
    return arenas.keySet().stream().sorted().collect(Collectors.toList());
  }

  public Stream<Arena> stream() {
    return arenas.values().stream();
  }

  public int size() {
    return arenas.size();
  }

  public void add(Arena arena) {
    arenas.putIfAbsent(arena.name(), arena);
  }

  public boolean remove(String name) {
    Arena arena = arenas.remove(name);
    if (arena != null) {
      arena.forEach(plugin.chunkManager()::cancel);
    }
    return GaiaIO.instance().deleteArena(name); // Cleanup files
  }

  public void cancelRevert(Arena arena) {
    arena.reverting(false);
    arena.forEach(GaiaChunk::cancelReverting);
  }

  public abstract void revert(GaiaUser user, Arena arena);

  public abstract boolean create(GaiaUser user, String arenaName);

  public abstract long nextRevertTime(Arena arena);

  public abstract @Nullable Arena standingArena(GaiaUser user);

  public Iterator<Arena> iterator() {
    return Collections.unmodifiableCollection(arenas.values()).iterator();
  }

  protected boolean splitIntoChunks(Arena arena) {
    final int minX = arena.region().min().getX();
    final int maxX = arena.region().max().getX();
    final int minY = arena.region().min().getY();
    final int maxY = arena.region().max().getY();
    final int minZ = arena.region().min().getZ();
    final int maxZ = arena.region().max().getZ();

    int tempX, tempZ;
    BlockVector3 v1, v2;
    for (int x = minX >> 4; x <= maxX >> 4; ++x) {
      tempX = x * 16;
      for (int z = minZ >> 4; z <= maxZ >> 4; ++z) {
        tempZ = z * 16;
        v1 = atXZClamped(tempX, minY, tempZ, minX, maxX, minZ, maxZ);
        v2 = atXZClamped(tempX + 15, maxY, tempZ + 15, minX, maxX, minZ, maxZ);
        final GaiaChunk chunkRegion = new GaiaChunk(UUID.randomUUID(), arena, new GaiaRegion(v1, v2));
        plugin.chunkManager().analyze(chunkRegion, arena.world());
      }
    }
    return arena.amount() > 0;
  }

  private BlockVector3 atXZClamped(int x, int y, int z, int minX, int maxX, int minZ, int maxZ) {
    if (minX > maxX || minZ > maxZ) {
      throw new IllegalArgumentException("Minimum cannot be greater than maximum");
    }
    return BlockVector3.at(Math.max(minX, Math.min(maxX, x)), y, Math.max(minZ, Math.min(maxZ, z)));
  }
}
