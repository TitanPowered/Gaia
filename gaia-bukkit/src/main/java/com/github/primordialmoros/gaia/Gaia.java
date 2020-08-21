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

package com.github.primordialmoros.gaia;

import com.github.primordialmoros.gaia.commands.CancelCommand;
import com.github.primordialmoros.gaia.commands.CreateCommand;
import com.github.primordialmoros.gaia.commands.GaiaCommand;
import com.github.primordialmoros.gaia.commands.InfoCommand;
import com.github.primordialmoros.gaia.commands.ListCommand;
import com.github.primordialmoros.gaia.commands.RemoveCommand;
import com.github.primordialmoros.gaia.commands.RevertCommand;
import com.github.primordialmoros.gaia.commands.VersionCommand;
import com.github.primordialmoros.gaia.configuration.ConfigManager;
import com.github.primordialmoros.gaia.implementation.ArenaManager;
import com.github.primordialmoros.gaia.implementation.GaiaChunkFactory;
import com.github.primordialmoros.gaia.io.GaiaIO;
import com.github.primordialmoros.gaia.platform.BlockDataWrapper;
import com.github.primordialmoros.gaia.platform.GaiaSender;
import com.github.primordialmoros.gaia.platform.PlayerWrapper;
import com.github.primordialmoros.gaia.platform.SenderWrapper;
import com.github.primordialmoros.gaia.platform.WorldWrapper;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Gaia extends JavaPlugin implements GaiaPlugin {
	public static final TextComponent PREFIX = TextComponent.builder("[", NamedTextColor.DARK_GRAY)
		.append("Gaia", NamedTextColor.DARK_AQUA)
		.append("] ", NamedTextColor.DARK_GRAY).build();

	private static Gaia plugin;
	private static BukkitAudiences audiences;
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
	public GaiaChunkFactory getChunkFactory() {
		return new GaiaChunkFactory();
	}

	@Override
	public ArenaManager getArenaManager() {
		return arenaManager;
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

	public void registerCommands() {
		new ListCommand(plugin);
		new InfoCommand(plugin);
		new VersionCommand(plugin);
		new CreateCommand(plugin);
		new RemoveCommand(plugin);
		new RevertCommand(plugin);
		new CancelCommand(plugin);

		final PluginCommand baseCommand = Gaia.getPlugin().getCommand("gaia");
		if (baseCommand == null) return;

		final CommandExecutor executor = (sender, command, alias, args) -> {
			final GaiaSender wrappedSender = sender instanceof Player ? new PlayerWrapper((Player) sender) : new SenderWrapper(sender);
			if (GaiaCommand.gaiaAliases.contains(alias.toLowerCase())) {
				if (args.length > 0) {
					final String argument = args[0].toLowerCase();
					Optional<GaiaCommand> cmd = GaiaCommand.commands.values().stream().filter(c -> c.getAliases().contains(argument)).findFirst();
					if (cmd.isPresent()) return cmd.get().execute(wrappedSender, Arrays.asList(args).subList(1, args.length));
				}
				GaiaCommand.viewHelp(new SenderWrapper(sender));
				return true;
			}
			return false;
		};

		final TabCompleter completer = (sender, command, alias, args) -> {
			final String argument = args.length > 0 ? args[0].toLowerCase() : "";
			if (args.length <= 1) {
				if (argument.isEmpty()) return new ArrayList<>(GaiaCommand.commands.keySet());
				return GaiaCommand.getFlatStream().filter(s -> s.startsWith(argument)).collect(Collectors.toList());
			} else if (args.length == 2) {
				Optional<GaiaCommand> cmd = GaiaCommand.commands.values().stream().filter(c -> c.completeArenaNames() && c.getAliases().contains(argument)).findFirst();
				if (cmd.isPresent()) return getArenaManager().getSortedArenaNames();
			}
			return new ArrayList<>();
		};

		baseCommand.setExecutor(executor);
		baseCommand.setTabCompleter(completer);
	}
}
