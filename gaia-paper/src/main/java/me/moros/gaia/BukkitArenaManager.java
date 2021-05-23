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

import java.util.UUID;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaRegion;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.locale.Message;
import me.moros.gaia.util.metadata.ArenaMetadata;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BukkitArenaManager extends ArenaManager {
  BukkitArenaManager(@NonNull GaiaPlugin plugin) {
    super(plugin);
  }

  @Override
  public void revertArena(@NonNull GaiaUser user, @NonNull Arena arena) {
    long startTime = System.currentTimeMillis();
    arena.setReverting(true);
    arena.getSubRegions().forEach(gcr -> Gaia.getPlugin().getChunkManager().revertChunk(gcr, arena.getWorld()));
    Bukkit.getScheduler().runTaskTimer(Gaia.getPlugin(), task -> {
      if (!arena.isReverting()) {
        Message.CANCEL_SUCCESS.send(user, arena.getFormattedName());
        task.cancel();
      } else {
        if (arena.getSubRegions().stream().noneMatch(GaiaChunk::isReverting)) {
          final long deltaTime = System.currentTimeMillis() - startTime;
          Message.FINISHED_REVERT.send(user, arena.getFormattedName(), String.valueOf(deltaTime));
          arena.setReverting(false);
          task.cancel();
        }
      }
    }, 1, 1);
  }

  @Override
  public boolean createArena(@NonNull GaiaUser user, @NonNull String arenaName) {
    if (!user.isPlayer()) {
      return false;
    }
    org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((BukkitGaiaUser) user).getHandle();
    UUID worldId = bukkitPlayer.getWorld().getUID();
    Player player = BukkitAdapter.adapt(bukkitPlayer);
    final Region r;
    final World world = player.getWorld();
    try {
      r = WorldEdit.getInstance().getSessionManager().get(player).getSelection(world);
    } catch (IncompleteRegionException e) {
      Message.CREATE_ERROR_SELECTION.send(user);
      return false;
    }

    if (!(r instanceof CuboidRegion)) {
      Message.CREATE_ERROR_CUBOID.send(user);
      return false;
    }
    int radius = Math.max(Math.max(r.getLength(), r.getWidth()), Math.max(r.getHeight(), 64));
    if (radius > 1024) { // For safety reasons limit to maximum 64 chunks in any direction
      Message.CREATE_ERROR_SIZE.send(user);
      return false;
    }
    if (r.getCenter().distanceSq(player.getLocation().toVector()) > radius * radius) {
      Message.CREATE_ERROR_DISTANCE.send(user);
      return false;
    }

    final BlockVector3 min = BlockVector3.at(r.getMinimumPoint().getX(), r.getMinimumPoint().getY(), r.getMinimumPoint().getZ());
    final BlockVector3 max = BlockVector3.at(r.getMaximumPoint().getX(), r.getMaximumPoint().getY(), r.getMaximumPoint().getZ());
    final GaiaRegion gr = new GaiaRegion(min, max);

    if (!isUniqueRegion(worldId, gr)) {
      Message.CREATE_ERROR_INTERSECTION.send(user);
      return false;
    }
    final Arena arena = new Arena(arenaName, world, worldId, gr);
    if (!GaiaIO.getInstance().createArenaFiles(arenaName)) {
      Message.CREATE_ERROR_CRITICAL.send(user);
      return false;
    }
    Message.CREATE_ANALYZING.send(user, arena.getFormattedName());
    if (!splitIntoChunks(arena)) {
      arena.getSubRegions().forEach(Gaia.getPlugin().getChunkManager()::cancelTasks);
      Message.CREATE_FAIL.send(user, arena.getFormattedName());
      return false;
    }
    arena.setMetadata(new ArenaMetadata(arena));
    final long timeout = System.currentTimeMillis() + Gaia.getPlugin().getConfig().getLong("Analysis.Timeout");
    Bukkit.getScheduler().runTaskTimer(Gaia.getPlugin(), task -> {
      if (System.currentTimeMillis() > timeout) {
        arena.getSubRegions().forEach(Gaia.getPlugin().getChunkManager()::cancelTasks);
        Message.CREATE_FAIL_TIMEOUT.send(user, arena.getFormattedName());
        removeArena(arena.getName());
        task.cancel();
      } else {
        if (arena.getSubRegions().stream().allMatch(GaiaChunk::isAnalyzed) && arena.finalizeArena()) {
          GaiaIO.getInstance().saveArena(arena).thenAccept(result -> {
            if (result) {
              Message.CREATE_SUCCESS.send(user, arena.getFormattedName());
            } else {
              arena.getSubRegions().forEach(Gaia.getPlugin().getChunkManager()::cancelTasks);
              Message.CREATE_FAIL.send(user, arena.getFormattedName());
            }
          });
          task.cancel();
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
    BlockVector3 v1, v2;
    for (int x = minX >> 4; x <= maxX >> 4; ++x) {
      tempX = x * 16;
      for (int z = minZ >> 4; z <= maxZ >> 4; ++z) {
        tempZ = z * 16;
        v1 = atXZClamped(tempX, minY, tempZ, minX, maxX, minZ, maxZ);
        v2 = atXZClamped(tempX + 15, maxY, tempZ + 15, minX, maxX, minZ, maxZ);
        final GaiaChunk chunkRegion = new GaiaChunk(UUID.randomUUID(), arena, new GaiaRegion(v1, v2));
        Gaia.getPlugin().getChunkManager().analyzeChunk(chunkRegion, arena.getWorld());
      }
    }
    return !arena.getSubRegions().isEmpty();
  }

  private static BlockVector3 atXZClamped(int x, int y, int z, int minX, int maxX, int minZ, int maxZ) {
    if (minX > maxX || minZ > maxZ) {
      throw new IllegalArgumentException("Minimum cannot be greater than maximum");
    }
    return BlockVector3.at(Math.max(minX, Math.min(maxX, x)), y, Math.max(minZ, Math.min(maxZ, z)));
  }
}
