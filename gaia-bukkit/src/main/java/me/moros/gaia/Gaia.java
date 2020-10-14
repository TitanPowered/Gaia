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

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.CommandContexts;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import io.papermc.lib.PaperLib;
import me.moros.gaia.api.Arena;
import me.moros.gaia.commands.GaiaCommand;
import me.moros.gaia.configuration.ConfigManager;
import me.moros.gaia.implementation.ArenaManager;
import me.moros.gaia.implementation.GaiaChunkFactory;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.platform.BlockDataWrapper;
import me.moros.gaia.platform.GaiaPlayer;
import me.moros.gaia.platform.GaiaSender;
import me.moros.gaia.platform.PlayerWrapper;
import me.moros.gaia.platform.SenderWrapper;
import me.moros.gaia.platform.WorldWrapper;
import me.moros.gaia.util.Util;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class Gaia extends JavaPlugin implements GaiaPlugin {
	public static final TextComponent PREFIX = Component.text("[", NamedTextColor.DARK_GRAY)
		.append(Component.text("Gaia", NamedTextColor.DARK_AQUA))
		.append(Component.text("] ", NamedTextColor.DARK_GRAY));

	private static Gaia plugin;
	private static BukkitAudiences audiences;
	private PaperCommandManager commandManager;
	private ArenaManager arenaManager;
	private String author;
	private String version;
	private Logger log;

	@Override
	public void onEnable() {
		new MetricsLite(this, 8608);
		PaperLib.suggestPaper(this);
		plugin = this;
		log = getLogger();
		version = getDescription().getVersion();
		author = getDescription().getAuthors().get(0);

		audiences = BukkitAudiences.create(this);

		new ConfigManager();

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

	public static BukkitAudiences getAudiences() {
		return audiences;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public Logger getLog() {
		return log;
	}

	@Override
	public Audience getAudience(CommandIssuer issuer) {
		return audiences.sender(issuer.getIssuer());
	}

	@Override
	public ArenaManager getArenaManager() {
		return arenaManager;
	}


	@Override
	public GaiaChunkFactory getChunkFactory() {
		return new GaiaChunkFactory();
	}

	@Override
	public BlockDataWrapper getBlockDataFromString(final String value) {
		try {
			return new BlockDataWrapper(Bukkit.createBlockData(value));
		} catch (IllegalArgumentException e) {
			getLog().warning("Invalid block data in palette: " + value + ". Block will be replaced with air.");
			return new BlockDataWrapper(Material.AIR.createBlockData());
		}
	}

	@Override
	public WorldWrapper getWorld(final UUID uid) {
		final World world = Bukkit.getWorld(uid);
		if (world == null) throw new IllegalArgumentException("Couldn't find world with UID " + uid);
		return new WorldWrapper(world);
	}

	public PaperCommandManager getCommandManager() {
		return commandManager;
	}

	private void registerCommands() {
		commandManager = new PaperCommandManager(plugin);
		commandManager.registerDependency(GaiaPlugin.class, plugin);
		commandManager.enableUnstableAPI("help");
		registerCommandContexts();
		registerCommandCompletions();
		Gaia.getPlugin().getCommandManager().getCommandReplacements().addReplacement("gaiacommand", "gaia|g|arena|arenas");
		Gaia.getPlugin().getCommandManager().registerCommand(new GaiaCommand());
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

		commandContexts.registerIssuerOnlyContext(GaiaSender.class, c -> new SenderWrapper(c.getSender()));

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