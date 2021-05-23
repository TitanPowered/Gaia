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

package me.moros.gaia;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.sk89q.worldedit.math.BlockVector3;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaRegion;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.io.GaiaIO;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class ArenaManager {
  private final Map<String, Arena> ARENAS = new ConcurrentHashMap<>();

  private final GaiaPlugin plugin;
  protected ArenaManager(@NonNull GaiaPlugin plugin) {
    this.plugin = plugin;
  }

  public Optional<Arena> getArena(@NonNull String name) {
    return Optional.ofNullable(ARENAS.get(name));
  }

  public boolean arenaExists(@NonNull String name) {
    return ARENAS.containsKey(name) || GaiaIO.getInstance().arenaFileExists(name);
  }

  public @NonNull List<@NonNull String> getSortedArenaNames() {
    return ARENAS.keySet().stream().sorted().collect(Collectors.toList());
  }

  public @NonNull Collection<@NonNull Arena> getAllArenas() {
    return ARENAS.values();
  }

  public int getArenaCount() {
    return ARENAS.size();
  }

  public void addArena(@NonNull Arena arena) {
    ARENAS.putIfAbsent(arena.getName(), arena);
  }

  public boolean removeArena(@NonNull String name) {
    Arena arena = ARENAS.remove(name);
    if (arena != null) {
      arena.getSubRegions().forEach(plugin.getChunkManager()::cancelTasks);
    }
    return GaiaIO.getInstance().deleteArena(name); // Cleanup files
  }

  public void cancelRevertArena(@NonNull Arena arena) {
    arena.setReverting(false);
    arena.getSubRegions().forEach(GaiaChunk::cancelReverting);
  }

  public Optional<Arena> getArenaAtPoint(@NonNull UUID id, @NonNull BlockVector3 point) {
    Objects.requireNonNull(point);
    return getAllArenas().stream().filter(a -> a.getWorldUID().equals(id) && a.getRegion().contains(point)).findAny();
  }

  public boolean isUniqueRegion(@NonNull UUID id, @NonNull GaiaRegion rg) {
    Objects.requireNonNull(rg);
    return ARENAS.values().stream().filter(a -> a.getWorldUID().equals(id)).map(Arena::getRegion).noneMatch(rg::intersects);
  }

  public abstract void revertArena(@NonNull GaiaUser user, @NonNull Arena arena);

  public abstract boolean createArena(@NonNull GaiaUser user, @NonNull String arenaName);
}
