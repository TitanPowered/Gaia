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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.CommandContexts;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.commands.GaiaCommand;
import me.moros.gaia.configuration.ConfigManager;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.locale.TranslationManager;
import me.moros.gaia.util.Util;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Gaia extends JavaPlugin implements GaiaPlugin {
  private BlockState AIR;

  private static Gaia plugin;
  private ExecutorService executor;
  private ParserContext parserContext;
  private PaperCommandManager commandManager;
  private BukkitArenaManager arenaManager;
  private BukkitChunkManager chunkManager;
  private String author;
  private String version;
  private Logger log;

  @Override
  public void onEnable() {
    new Metrics(this, 8608);
    plugin = this;
    log = getLogger();
    version = getDescription().getVersion();
    author = getDescription().getAuthors().get(0);

    ConfigManager.INSTANCE.init();

    executor = Executors.newFixedThreadPool(ConfigManager.INSTANCE.getConcurrentChunks());
    parserContext = new ParserContext();
    parserContext.setRestricted(false);
    parserContext.setTryLegacy(false);
    parserContext.setPreferringWildcard(false);
    AIR = BukkitAdapter.adapt(Material.AIR.createBlockData());

    new TranslationManager(log, getDataFolder().toString());

    arenaManager = new BukkitArenaManager(this);
    chunkManager = new BukkitChunkManager();

    boolean debug = getConfig().getBoolean("Debug");
    if (debug) {
      getLog().info("Debugging is enabled");
    }
    if (!GaiaIO.createInstance(plugin, getDataFolder().getPath(), debug)) {
      getLog().severe("Could not create Arenas folder! Aborting plugin load.");
      plugin.setEnabled(false);
      return;
    }
    long startTime = System.currentTimeMillis();
    GaiaIO.getInstance().loadAllArenas().thenRun(() -> {
      long delta = System.currentTimeMillis() - startTime;
      plugin.getLog().info("Successfully loaded " + arenaManager.getArenaCount() + " arenas in (" + delta + "ms)");
    });
    registerCommands();
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

  public static Gaia getPlugin() {
    return plugin;
  }

  @Override
  public @NonNull String getAuthor() {
    return author;
  }

  @Override
  public @NonNull String getVersion() {
    return version;
  }

  @Override
  public @NonNull Logger getLog() {
    return log;
  }

  @Override
  public @NonNull BukkitArenaManager getArenaManager() {
    return arenaManager;
  }

  @Override
  public @NonNull BukkitChunkManager getChunkManager() {
    return chunkManager;
  }

  @Override
  public @NonNull BlockState getBlockDataFromString(@Nullable String value) {
    if (value != null) {
      try {
        return WorldEdit.getInstance().getBlockFactory().parseFromInput(value, parserContext).toImmutableState();
      } catch (InputParseException e) {
        log.warning("Invalid BlockState in palette: " + value + ". Block will be replaced with air.");
      }
    }
    return AIR;
  }

  @Override
  public @Nullable World getWorld(@NonNull UUID uid) {
    final org.bukkit.World world = Bukkit.getWorld(uid);
    if (world == null) {
      log.warning("Couldn't find world with UID " + uid);
      return null;
    }
    return BukkitAdapter.adapt(world);
  }

  @Override
  public @NonNull Executor executor() {
    return executor;
  }

  private void registerCommands() {
    commandManager = new PaperCommandManager(plugin);
    commandManager.registerDependency(GaiaPlugin.class, plugin);
    commandManager.enableUnstableAPI("help");
    registerCommandContexts();
    registerCommandCompletions();
    commandManager.getCommandReplacements().addReplacement("gaiacommand", "gaia|g|arena|arenas");
    commandManager.registerCommand(new GaiaCommand());
  }

  private void registerCommandCompletions() {
    commandManager.getCommandCompletions().registerAsyncCompletion("arenas", c ->
      getArenaManager().getSortedArenaNames()
    );
  }

  private void registerCommandContexts() {
    CommandContexts<BukkitCommandExecutionContext> commandContexts = commandManager.getCommandContexts();

    commandContexts.registerIssuerOnlyContext(GaiaUser.class, c -> new BukkitGaiaUser(c.getSender()));

    commandContexts.registerIssuerAwareContext(Arena.class, c -> {
      String name = c.popFirstArg();
      if (name != null) {
        String sanitized = Util.sanitizeInput(name);
        return getArenaManager().getArena(sanitized)
          .orElseThrow(() -> new InvalidCommandArgument("Could not find arena " + sanitized));
      }
      if (c.hasFlag("standing")) {
        org.bukkit.entity.Player p = c.getPlayer();
        if (p != null) {
          Player player = BukkitAdapter.adapt(p);
          UUID worldId = p.getWorld().getUID();
          return getArenaManager().getArenaAtPoint(worldId, player.getLocation().toVector().toBlockPoint())
            .orElseThrow(() -> new InvalidCommandArgument("You are not currently standing in an arena."));
        }
      }
      throw new InvalidCommandArgument("Could not find a valid arena.");
    });
  }
}
