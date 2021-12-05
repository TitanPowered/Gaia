/*
 * Copyright 2020-2021 Moros
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
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurationNode;

public class BukkitArenaManager extends ArenaManager {
  private final long timeout;
  private final long cooldown;

  BukkitArenaManager(@NonNull Gaia plugin) {
    super(plugin);
    ConfigurationNode config = plugin.configManager().config();
    this.timeout = config.node("Analysis", "Timeout").getLong(30_000);
    this.cooldown = config.node("Cooldown").getLong(5000);
  }

  @Override
  public void revert(@NonNull GaiaUser user, @NonNull Arena arena) {
    long startTime = System.currentTimeMillis();
    arena.reverting(true);
    arena.forEach(gcr -> plugin.chunkManager().revert(gcr, arena.world()));
    Bukkit.getScheduler().runTaskTimer((Plugin) plugin, task -> {
      if (!arena.reverting()) {
        Message.CANCEL_SUCCESS.send(user, arena.displayName());
        task.cancel();
      } else {
        if (arena.stream().noneMatch(GaiaChunk::reverting)) {
          final long deltaTime = System.currentTimeMillis() - startTime;
          Message.FINISHED_REVERT.send(user, arena.displayName(), String.valueOf(deltaTime));
          arena.reverting(false);
          task.cancel();
        }
      }
    }, 1, 1);
  }

  @Override
  public boolean create(@NonNull GaiaUser user, @NonNull String arenaName) {
    if (!user.isPlayer()) {
      return false;
    }
    org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((BukkitGaiaUser) user).sender();
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

    if (stream().filter(a -> a.worldUID().equals(worldId)).map(Arena::region).anyMatch(gr::intersects)) {
      Message.CREATE_ERROR_INTERSECTION.send(user);
      return false;
    }
    final Arena arena = new Arena(arenaName, world, worldId, gr);
    if (!GaiaIO.instance().createArenaFiles(arenaName)) {
      Message.CREATE_ERROR_CRITICAL.send(user);
      return false;
    }
    Message.CREATE_ANALYZING.send(user, arena.displayName());
    if (!splitIntoChunks(arena)) {
      arena.forEach(plugin.chunkManager()::cancel);
      Message.CREATE_FAIL.send(user, arena.displayName());
      return false;
    }
    arena.metadata(new ArenaMetadata(arena));
    final long timeoutMoment = System.currentTimeMillis() + this.timeout;
    Bukkit.getScheduler().runTaskTimer((Plugin) plugin, task -> {
      if (System.currentTimeMillis() > timeoutMoment) {
        arena.forEach(plugin.chunkManager()::cancel);
        Message.CREATE_FAIL_TIMEOUT.send(user, arena.displayName());
        remove(arena.name());
        task.cancel();
      } else {
        if (arena.stream().allMatch(GaiaChunk::analyzed) && arena.finalizeArena()) {
          GaiaIO.instance().saveArena(arena).thenAccept(result -> {
            if (result) {
              Message.CREATE_SUCCESS.send(user, arena.displayName());
            } else {
              arena.forEach(plugin.chunkManager()::cancel);
              Message.CREATE_FAIL.send(user, arena.displayName());
            }
          });
          task.cancel();
        }
      }
    }, 1, 1);
    add(arena);
    return true;
  }

  @Override
  public long nextRevertTime(@NonNull Arena arena) {
    return arena.lastReverted() + cooldown;
  }

  private boolean splitIntoChunks(@NonNull Arena arena) {
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

  private static BlockVector3 atXZClamped(int x, int y, int z, int minX, int maxX, int minZ, int maxZ) {
    if (minX > maxX || minZ > maxZ) {
      throw new IllegalArgumentException("Minimum cannot be greater than maximum");
    }
    return BlockVector3.at(Math.max(minX, Math.min(maxX, x)), y, Math.max(minZ, Math.min(maxZ, z)));
  }
}
