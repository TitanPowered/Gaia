/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *
 * 	  This file is part of Gaia.
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

import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaRegion;
import me.moros.gaia.api.GaiaVector;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.platform.GaiaPlayer;
import me.moros.gaia.util.functional.GaiaConsumerInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class GaiaArenaManager {
	private final Map<String, Arena> ARENAS = new ConcurrentHashMap<>();

	public Arena getArena(final String name) {
		return ARENAS.get(name);
	}

	public boolean arenaExists(final String name) {
		return ARENAS.containsKey(name) || GaiaIO.getInstance().arenaFileExists(name);
	}

	public List<String> getSortedArenaNames() {
		return ARENAS.keySet().stream().sorted().collect(Collectors.toList());
	}

	public Collection<Arena> getAllArenas() {
		return ARENAS.values();
	}

	public int getArenaCount() {
		return ARENAS.size();
	}

	public void addArena(final Arena arena) {
		if (arena != null && !ARENAS.containsKey(arena.getName())) ARENAS.put(arena.getName(), arena);
	}

	public boolean removeArena(final String name) {
		ARENAS.remove(name);
		return GaiaIO.getInstance().deleteArena(name); // Cleanup files
	}

	public void cancelRevertArena(final Arena arena) {
		arena.setReverting(false);
		arena.getSubRegions().forEach(GaiaChunk::cancelReverting);
	}

	public Optional<Arena> getArenaAtPoint(final UUID id, final GaiaVector l) {
		return getAllArenas().stream().filter(a -> a.getWorldUID().equals(id) && a.getRegion().contains(l)).findAny();
	}

	public boolean isUniqueRegion(final UUID id, final GaiaRegion rg) {
		return ARENAS.values().stream().filter(a -> a.getWorldUID().equals(id)).map(Arena::getRegion).noneMatch(rg::intersects);
	}

	public abstract void revertArena(final Arena arena, final GaiaConsumerInfo info);

	public abstract boolean createArena(final GaiaPlayer player, final String arenaName);
}
