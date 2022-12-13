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
import me.moros.gaia.event.ArenaAnalyzeEvent;
import me.moros.gaia.event.ArenaRevertEvent;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.locale.Message;
import me.moros.gaia.util.metadata.ArenaMetadata;
import net.kyori.adventure.identity.Identity;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BukkitArenaManager extends ArenaManager {
  BukkitArenaManager(GaiaPlugin plugin) {
    super(plugin);
  }

  @Override
  public void revert(GaiaUser user, Arena arena) {
    long startTime = System.currentTimeMillis();
    arena.reverting(true);
    arena.forEach(gcr -> plugin.chunkManager().revert(gcr, arena.world()));
    Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin) plugin, task -> {
      if (!arena.reverting()) {
        final long deltaTime = System.currentTimeMillis() - startTime;
        Message.CANCEL_SUCCESS.send(user, arena.displayName());
        task.cancel();
        WorldEdit.getInstance().getEventBus().post(new ArenaRevertEvent(arena, deltaTime, true));
      } else {
        if (arena.stream().noneMatch(GaiaChunk::reverting)) {
          final long deltaTime = System.currentTimeMillis() - startTime;
          Message.FINISHED_REVERT.send(user, arena.displayName(), deltaTime);
          arena.reverting(false);
          task.cancel();
          WorldEdit.getInstance().getEventBus().post(new ArenaRevertEvent(arena, deltaTime));
        }
      }
    }, 1, 1);
  }

  @Override
  public boolean create(GaiaUser user, String arenaName) {
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
    long startTime = System.currentTimeMillis();
    if (!splitIntoChunks(arena)) {
      arena.forEach(plugin.chunkManager()::cancel);
      Message.CREATE_FAIL.send(user, arena.displayName());
      return false;
    }
    arena.metadata(new ArenaMetadata(arena));
    final long timeoutMoment = startTime + plugin.configManager().config().timeout();
    Bukkit.getScheduler().runTaskTimer((Plugin) plugin, task -> {
      final long time = System.currentTimeMillis();
      if (time > timeoutMoment) {
        arena.forEach(plugin.chunkManager()::cancel);
        Message.CREATE_FAIL_TIMEOUT.send(user, arena.displayName());
        remove(arena.name());
        task.cancel();
      } else {
        if (arena.stream().allMatch(GaiaChunk::analyzed) && arena.finalizeArena()) {
          WorldEdit.getInstance().getEventBus().post(new ArenaAnalyzeEvent(arena, time - startTime));
          GaiaIO.instance().saveArena(arena).thenAccept(result -> {
            if (result) {
              Message.CREATE_SUCCESS.send(user, arena.displayName());
            } else {
              remove(arena.name());
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
  public long nextRevertTime(Arena arena) {
    return arena.lastReverted() + plugin.configManager().config().cooldown();
  }

  @Override
  public @Nullable Arena standingArena(GaiaUser user) {
    if (!user.isPlayer()) {
      return null;
    }
    org.bukkit.entity.Player bukkitPlayer = user.pointers().get(Identity.UUID).map(Bukkit::getPlayer)
      .orElseThrow(() -> new IllegalArgumentException("User is not a valid player!"));
    UUID worldId = bukkitPlayer.getWorld().getUID();
    BlockVector3 point = BukkitAdapter.adapt(bukkitPlayer).getLocation().toVector().toBlockPoint();
    return plugin.arenaManager().stream()
      .filter(a -> a.worldUID().equals(worldId) && a.region().contains(point)).findAny().orElse(null);
  }
}
