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

import com.github.primordialmoros.gaia.commands.GaiaCommand;
import com.github.primordialmoros.gaia.configuration.ConfigManager;
import com.github.primordialmoros.gaia.io.GaiaIO;
import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Gaia extends JavaPlugin {
	public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "Gaia" + ChatColor.DARK_GRAY + "] ";

	private static Gaia plugin;
    private static String author;
    private static String version;
    private static Logger log;

    @Override
    public void onEnable() {
		PaperLib.suggestPaper(this);
        plugin = this;
        log = getLogger();
        version = getDescription().getVersion();
        author = getDescription().getAuthors().get(0);

        new ConfigManager();

        boolean debug = getConfig().getBoolean("Debug");
        if (debug) getLog().info("Debugging is enabled");
		if (!GaiaIO.createInstance(getDataFolder().getPath(), debug)) {
			Gaia.getLog().severe("Could not create Arenas folder! Aborting plugin load.");
			Gaia.getPlugin().setEnabled(false);
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(Gaia.getPlugin(), GaiaIO.getInstance()::loadAllArenas);
		GaiaCommand.registerCommands();
    }

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
	}

    public static Gaia getPlugin() {
    	return plugin;
	}

	public static String getAuthor() {
		return author;
	}

	public static String getVersion() {
		return version;
	}

	public static Logger getLog() {
		return log;
	}
}
