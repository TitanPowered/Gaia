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

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.CommandContexts;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaRegion;
import me.moros.gaia.commands.GaiaCommand;
import me.moros.gaia.configuration.ConfigManager;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.locale.TranslationManager;
import me.moros.gaia.platform.BlockDataWrapper;
import me.moros.gaia.platform.GaiaPlayer;
import me.moros.gaia.platform.GaiaUser;
import me.moros.gaia.platform.PlayerWrapper;
import me.moros.gaia.platform.UserWrapper;
import me.moros.gaia.platform.WorldWrapper;
import me.moros.gaia.util.Util;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Gaia extends JavaPlugin implements GaiaPlugin {
  private static Gaia plugin;
  private PaperCommandManager commandManager;
  private ArenaManager arenaManager;
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

    new TranslationManager(log, getDataFolder().toString());

    arenaManager = new ArenaManager();
    boolean debug = getConfig().getBoolean("Debug");
    if (debug) getLog().info("Debugging is enabled");
    if (!GaiaIO.createInstance(plugin, getDataFolder().getPath(), debug)) {
      getLog().severe("Could not create Arenas folder! Aborting plugin load.");
      plugin.setEnabled(false);
      return;
    }
    Bukkit.getScheduler().runTaskAsynchronously(Gaia.getPlugin(), GaiaIO.getInstance()::loadAllArenas);
    registerCommands();
  }

  @Override
  public void onDisable() {
    getServer().getScheduler().cancelTasks(this);
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
  public @NonNull ArenaManager getArenaManager() {
    return arenaManager;
  }

  @Override
  public @NonNull PaperGaiaChunk adaptChunk(@NonNull UUID id, @NonNull Arena parent, @NonNull GaiaRegion region) {
    return new PaperGaiaChunk(id, parent, region);
  }

  @Override
  public @NonNull BlockDataWrapper getBlockDataFromString(final String value) {
    if (value != null) {
      try {
        BlockData data = Bukkit.createBlockData(value);
        return new BlockDataWrapper(data);
      } catch (IllegalArgumentException e) {
        log.warning("Invalid block data in palette: " + value + ". Block will be replaced with air.");
      } catch (Exception other) {
        // do nothing
      }
    }
    return new BlockDataWrapper(Material.AIR.createBlockData());
  }

  @Override
  public @Nullable WorldWrapper getWorld(final UUID uid) {
    final World world = Bukkit.getWorld(uid);
    if (world == null) {
      log.warning("Couldn't find world with UID " + uid);
      return null;
    }
    return new WorldWrapper(world);
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
    commandContexts.registerIssuerOnlyContext(GaiaPlayer.class, c -> {
      Player player = c.getPlayer();
      if (player == null) throw new InvalidCommandArgument("You must be player!");
      return new PlayerWrapper(player);
    });

    commandContexts.registerIssuerOnlyContext(GaiaUser.class, c -> new UserWrapper(c.getSender()));

    commandContexts.registerIssuerAwareContext(Arena.class, c -> {
      String name = c.popFirstArg();
      if (name != null) {
        String sanitized = Util.sanitizeInput(name);
        return Optional.ofNullable(getArenaManager().getArena(sanitized))
          .orElseThrow(() -> new InvalidCommandArgument("Could not find arena " + sanitized));
      }
      if (c.hasFlag("standing")) {
        Player player = c.getPlayer();
        if (player != null) {
          GaiaPlayer gaiaPlayer = new PlayerWrapper(player);
          return getArenaManager().getArenaAtPoint(gaiaPlayer.getWorld().getUID(), gaiaPlayer.getLocation())
            .orElseThrow(() -> new InvalidCommandArgument("You are not currently standing in an arena."));
        }
      }
      throw new InvalidCommandArgument("Could not find a valid arena.");
    });
  }
}
