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

package me.moros.gaia;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import me.moros.gaia.api.ArenaPoint;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.command.Commander;
import me.moros.gaia.config.ConfigManager;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.locale.TranslationManager;
import me.moros.tasker.bukkit.BukkitExecutor;
import me.moros.tasker.executor.CompositeExecutor;
import me.moros.tasker.executor.SimpleAsyncExecutor;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

public class Gaia extends JavaPlugin implements GaiaPlugin {
  private BlockState AIR;

  private CompositeExecutor executor;
  private ParserContext parserContext;
  private ConfigManager configManager;
  private Commander commander;
  private TranslationManager translationManager;
  private ArenaManager arenaManager;
  private ChunkManager chunkManager;
  private String author;
  private String version;
  private Logger logger;

  @Override
  public void onEnable() {
    new Metrics(this, 8608);
    logger = getSLF4JLogger();
    version = getDescription().getVersion();
    author = getDescription().getAuthors().get(0);

    Path dir = getDataFolder().toPath();
    configManager = new ConfigManager(logger, dir);

    int threads = Math.max(8, 2 * configManager.config().concurrentChunks());
    var pool = Executors.newScheduledThreadPool(threads);
    executor = CompositeExecutor.of(new BukkitExecutor(this), new SimpleAsyncExecutor(pool));
    parserContext = new ParserContext();
    parserContext.setRestricted(false);
    parserContext.setTryLegacy(false);
    parserContext.setPreferringWildcard(false);
    AIR = BukkitAdapter.adapt(Material.AIR.createBlockData());

    translationManager = new TranslationManager(logger, dir);

    loadLightFixer(this);
    arenaManager = new ArenaManager(this);
    chunkManager = new BukkitChunkManager(this);

    if (!GaiaIO.createInstance(this, dir)) {
      logger.error("Could not create Arenas folder! Aborting plugin load.");
      setEnabled(false);
      return;
    }
    long startTime = System.currentTimeMillis();
    GaiaIO.instance().loadAllArenas().thenRun(() -> {
      long delta = System.currentTimeMillis() - startTime;
      int size = arenaManager.size();
      logger.info("Successfully loaded " + size + (size == 1 ? " arena" : " arenas") + " (" + delta + "ms)");
    });
    try {
      PaperCommandManager<GaiaUser> manager = new PaperCommandManager<>(this,
        CommandExecutionCoordinator.simpleCoordinator(),
        c -> new BukkitGaiaUser(this, c),
        u -> ((BukkitGaiaUser) u).sender()
      );
      manager.registerAsynchronousCompletions();
      commander = Commander.create(manager, this);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    configManager.save();
  }

  @Override
  public void onDisable() {
    executor.shutdown();
    getServer().getScheduler().cancelTasks(this);
    if (chunkManager != null) {
      chunkManager.shutdown();
    }
  }

  private void loadLightFixer(Gaia plugin) {
    String fullName = plugin.getServer().getClass().getPackageName();
    String nmsVersion = fullName.substring(1 + fullName.lastIndexOf("."));
    String className = "me.moros.gaia.nms." + nmsVersion + ".LightFixerImpl";
    try {
      Class<?> cls = Class.forName(className);
      if (!cls.isSynthetic() && LightFixer.class.isAssignableFrom(cls)) {
        new RevertListener(this, (LightFixer) cls.getDeclaredConstructor().newInstance());
      }
    } catch (Exception ignore) {
      String s = String.format("""

        ****************************************************************
        * Unable to find native module for version %s.
        * Chunk relighting will not be available during this session.
        ****************************************************************

        """, nmsVersion);
      plugin.logger().warn(s);
    }
  }

  @Override
  public String author() {
    return author;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public Logger logger() {
    return logger;
  }

  @Override
  public ConfigManager configManager() {
    return configManager;
  }

  @Override
  public TranslationManager translationManager() {
    return translationManager;
  }

  @Override
  public ArenaManager arenaManager() {
    return arenaManager;
  }

  @Override
  public ChunkManager chunkManager() {
    return chunkManager;
  }

  @Override
  public BlockState parseBlockData(@Nullable String value) {
    if (value != null) {
      try {
        return WorldEdit.getInstance().getBlockFactory().parseFromInput(value, parserContext).toImmutableState();
      } catch (InputParseException e) {
        logger.warn("Invalid BlockState in palette: " + value + ". Block will be replaced with air.");
      }
    }
    return AIR;
  }

  @Override
  public @Nullable World findWorld(Key worldKey) {
    final org.bukkit.World world = Bukkit.getWorld(new NamespacedKey(worldKey.namespace(), worldKey.value()));
    if (world == null) {
      logger.warn("Couldn't find world with key " + worldKey);
      return null;
    }
    return BukkitAdapter.adapt(world);
  }

  @Override
  public @Nullable GaiaUser findUser(String input) {
    var player = getServer().getPlayer(input);
    if (player == null) {
      try {
        UUID uuid = UUID.fromString(input);
        player = getServer().getPlayer(uuid);
      } catch (Exception ignore) {
      }
    }
    return player == null ? null : new BukkitGaiaUser(this, player);
  }

  @Override
  public Stream<String> users() {
    return getServer().getOnlinePlayers().stream().map(Entity::getName);
  }

  @Override
  public CompositeExecutor executor() {
    return executor;
  }

  @Override
  public @Nullable ArenaPoint pointFromUser(GaiaUser user) {
    if (!user.isPlayer()) {
      return null;
    }
    var loc = adapt(user).getLocation();
    return new ArenaPoint(loc.toVector(), loc.getYaw(), loc.getPitch());
  }

  @Override
  public void teleport(GaiaUser user, Key worldKey, ArenaPoint point) {
    final org.bukkit.World world = Bukkit.getWorld(new NamespacedKey(worldKey.namespace(), worldKey.value()));
    if (!user.isPlayer() || world == null) {
      return;
    }
    Location loc = BukkitAdapter.adapt(world, point.v());
    loc.setYaw(point.yaw());
    loc.setPitch(point.pitch());
    BukkitAdapter.adapt(adapt(user)).teleportAsync(loc);
  }

  @Override
  public Player adapt(GaiaUser user) {
    return user.pointers().get(Identity.UUID)
      .map(getServer()::getPlayer)
      .map(BukkitAdapter::adapt)
      .orElseThrow(() -> new IllegalArgumentException("User is not a valid player!"));
  }
}
