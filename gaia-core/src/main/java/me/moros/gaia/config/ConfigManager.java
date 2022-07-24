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

package me.moros.gaia.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import me.moros.gaia.GaiaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

public final class ConfigManager {
  private final HoconConfigurationLoader loader;
  private final GaiaPlugin plugin;

  private CommentedConfigurationNode configRoot;
  private Config config;

  public ConfigManager(@NonNull GaiaPlugin plugin, @NonNull String directory) {
    this.plugin = plugin;
    Path path = Paths.get(directory, "gaia.conf");
    loader = HoconConfigurationLoader.builder().path(path).build();
    try {
      Files.createDirectories(path.getParent());
      reload();
    } catch (IOException e) {
      plugin.logger().warn(e.getMessage(), e);
    }
  }

  public void reload() {
    try {
      configRoot = loader.load();
      config = new Config(configRoot);
      plugin.logger().info("Debugging is " + (config.debug() ? "enabled." : "disabled."));
    } catch (IOException e) {
      plugin.logger().warn(e.getMessage(), e);
    }
  }

  public void save() {
    try {
      plugin.logger().info("Saving gaia config");
      loader.save(configRoot);
    } catch (IOException e) {
      plugin.logger().warn(e.getMessage(), e);
    }
  }

  public @NonNull Config config() {
    return config;
  }
}
