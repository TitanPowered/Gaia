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

import java.util.UUID;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaRegion;
import me.moros.gaia.api.GaiaVector;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.locale.Message;
import me.moros.gaia.platform.GaiaPlayer;
import me.moros.gaia.platform.PlayerWrapper;
import me.moros.gaia.platform.WorldWrapper;
import me.moros.gaia.util.functional.GaiaConsumerInfo;
import me.moros.gaia.util.metadata.ArenaMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ArenaManager extends GaiaArenaManager {
  @Override
  public void revertArena(@NonNull Arena arena, @NonNull GaiaConsumerInfo info) {
    arena.setReverting(true);
    arena.getSubRegions().forEach(gcr -> PaperGaiaChunk.revertChunk(gcr, arena.getWorld()));
    Bukkit.getScheduler().runTaskTimer(Gaia.getPlugin(), l -> {
      if (!arena.isReverting()) {
        Message.CANCEL_SUCCESS.send(info.user, arena.getFormattedName());
        l.cancel();
      } else {
        if (arena.getSubRegions().stream().noneMatch(GaiaChunk::isReverting)) {
          final long deltaTime = System.currentTimeMillis() - info.startTime;
          Message.FINISHED_REVERT.send(info.user, arena.getFormattedName(), String.valueOf(deltaTime));
          arena.setReverting(false);
          l.cancel();
        }
      }
    }, 1, 1);
  }

  @Override
  public boolean createArena(@NonNull GaiaPlayer user, @NonNull String arenaName) {
    final Region r;
    final Player player = ((PlayerWrapper) user).get();
    final WorldWrapper world = new WorldWrapper(player.getWorld());
    try {
      r = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player)).getSelection(BukkitAdapter.adapt(world.get()));
    } catch (IncompleteRegionException e) {
      Message.CREATE_ERROR_SELECTION.send(user);
      return false;
    }

    if (!(r instanceof CuboidRegion)) {
      Message.CREATE_ERROR_CUBOID.send(user);
      return false;
    }
    int radius = Math.max(Math.max(r.getLength(), r.getWidth()), Math.max(r.getHeight(), 64));
    if (radius > 512) { // For safety reasons
      Message.CREATE_ERROR_SIZE.send(user);
      return false;
    }
    if (r.getCenter().distanceSq(BukkitAdapter.adapt(player.getLocation()).toVector()) > radius * radius) {
      Message.CREATE_ERROR_DISTANCE.send(user);
      return false;
    }

    final GaiaVector min = GaiaVector.at(r.getMinimumPoint().getX(), r.getMinimumPoint().getY(), r.getMinimumPoint().getZ());
    final GaiaVector max = GaiaVector.at(r.getMaximumPoint().getX(), r.getMaximumPoint().getY(), r.getMaximumPoint().getZ());
    final GaiaRegion gr = new GaiaRegion(min, max);

    if (!isUniqueRegion(world.getUID(), gr)) {
      Message.CREATE_ERROR_INTERSECTION.send(user);
      return false;
    }
    final Arena arena = new Arena(arenaName, world, gr);
    if (!GaiaIO.getInstance().createArenaFiles(arenaName)) {
      Message.CREATE_ERROR_CRITICAL.send(user);
      return false;
    }
    Message.CREATE_ANALYZING.send(user, arena.getFormattedName());
    final GaiaConsumerInfo info = new GaiaConsumerInfo(user);
    if (!splitIntoChunks(arena)) {
      Message.CREATE_FAIL.send(user, arena.getFormattedName());
      return false;
    }
    arena.setMetadata(new ArenaMetadata(arena));
    final long timeout = Gaia.getPlugin().getConfig().getLong("Analysis.Timeout");
    Bukkit.getScheduler().runTaskTimer(Gaia.getPlugin(), l -> {
      if (System.currentTimeMillis() > info.startTime + timeout) {
        Message.CREATE_FAIL.send(user, arena.getFormattedName());
        removeArena(arena.getName());
        l.cancel();
      } else {
        if (arena.getSubRegions().stream().allMatch(GaiaChunk::isAnalyzed) && arena.finalizeArena()) {
          Bukkit.getScheduler().runTaskAsynchronously(Gaia.getPlugin(), () -> {
            if (GaiaIO.getInstance().saveArena((ArenaMetadata) arena.getMetadata())) {
              Message.CREATE_SUCCESS.send(user, arena.getFormattedName());
            } else {
              Message.CREATE_FAIL.send(user, arena.getFormattedName());
            }
          });
          l.cancel();
        }
      }
    }, 1, 1);
    addArena(arena);
    return true;
  }

  public static boolean splitIntoChunks(@NonNull Arena arena) {
    final int minX = arena.getRegion().getMinimumPoint().getX();
    final int maxX = arena.getRegion().getMaximumPoint().getX();
    final int minY = arena.getRegion().getMinimumPoint().getY();
    final int maxY = arena.getRegion().getMaximumPoint().getY();
    final int minZ = arena.getRegion().getMinimumPoint().getZ();
    final int maxZ = arena.getRegion().getMaximumPoint().getZ();

    int tempX, tempZ;
    GaiaVector v1, v2;
    for (int x = minX >> 4; x <= maxX >> 4; ++x) {
      tempX = x * 16;
      for (int z = minZ >> 4; z <= maxZ >> 4; ++z) {
        tempZ = z * 16;
        v1 = GaiaVector.atXZClamped(tempX, minY, tempZ, minX, maxX, minZ, maxZ);
        v2 = GaiaVector.atXZClamped(tempX + 15, maxY, tempZ + 15, minX, maxX, minZ, maxZ);
        final PaperGaiaChunk chunkRegion = Gaia.getPlugin().adaptChunk(UUID.randomUUID(), arena, new GaiaRegion(v1, v2));
        PaperGaiaChunk.analyzeChunk(chunkRegion, arena.getWorld());
      }
    }
    return !arena.getSubRegions().isEmpty();
  }
}
