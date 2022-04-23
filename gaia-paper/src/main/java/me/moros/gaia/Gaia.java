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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.config.ConfigManager;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.locale.TranslationManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

public class Gaia extends JavaPlugin implements GaiaPlugin {
  private BlockState AIR;

  private ExecutorService executor;
  private ParserContext parserContext;
  private ConfigManager configManager;
  private GaiaCommandManager commandManager;
  private BukkitArenaManager arenaManager;
  private BukkitChunkManager chunkManager;
  private String author;
  private String version;
  private Logger logger;

  @Override
  public void onEnable() {
    new Metrics(this, 8608);
    logger = getSLF4JLogger();
    version = getDescription().getVersion();
    author = getDescription().getAuthors().get(0);

    String dir = getDataFolder().toString();
    configManager = new ConfigManager(this, dir);

    executor = Executors.newFixedThreadPool(configManager.config().node("ConcurrentChunks").getInt(4));
    parserContext = new ParserContext();
    parserContext.setRestricted(false);
    parserContext.setTryLegacy(false);
    parserContext.setPreferringWildcard(false);
    AIR = BukkitAdapter.adapt(Material.AIR.createBlockData());

    new TranslationManager(logger, dir);

    arenaManager = new BukkitArenaManager(this);
    chunkManager = new BukkitChunkManager(this);

    boolean debug = configManager.config().node("Debug").getBoolean(false);
    if (debug) {
      logger.info("Debugging is enabled");
    }
    if (!GaiaIO.createInstance(this, getDataFolder().getPath(), debug)) {
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
      commandManager = new GaiaCommandManager(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
    configManager.save();
  }

  @Override
  public void onDisable() {
    executor.shutdown();
    try {
      executor.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    getServer().getScheduler().cancelTasks(this);
    if (chunkManager != null) {
      chunkManager.shutdown();
    }
  }

  @Override
  public @NonNull String author() {
    return author;
  }

  @Override
  public @NonNull String version() {
    return version;
  }

  @Override
  public @NonNull Logger logger() {
    return logger;
  }

  @Override
  public @NonNull ConfigManager configManager() {
    return configManager;
  }

  @Override
  public @NonNull BukkitArenaManager arenaManager() {
    return arenaManager;
  }

  @Override
  public @NonNull BukkitChunkManager chunkManager() {
    return chunkManager;
  }

  @Override
  public @NonNull BlockState parseBlockData(@Nullable String value) {
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
  public @Nullable World findWorld(@NonNull UUID uid) {
    final org.bukkit.World world = Bukkit.getWorld(uid);
    if (world == null) {
      logger.warn("Couldn't find world with UID " + uid);
      return null;
    }
    return BukkitAdapter.adapt(world);
  }

  @Override
  public @NonNull Executor executor() {
    return executor;
  }

  public void queryCommands(@NonNull String rawQuery, @NonNull GaiaUser recipient) {
    commandManager.help().queryCommands(rawQuery, recipient);
  }
}
