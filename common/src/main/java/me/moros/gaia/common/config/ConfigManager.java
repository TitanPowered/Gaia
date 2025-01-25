/*
 * Copyright 2020-2025 Moros
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

package me.moros.gaia.common.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.reference.WatchServiceListener;

public final class ConfigManager {
  private static ConfigManager INSTANCE;

  private final Config defaultConfig;

  private final ConfigurationReference<CommentedConfigurationNode> reference;
  private final ValueReference<Config, CommentedConfigurationNode> configReference;

  public ConfigManager(Logger logger, Path directory, WatchServiceListener listener) throws IOException {
    this.defaultConfig = new Config();

    Path path = directory.resolve("gaia.conf");
    Files.createDirectories(path.getParent());

    this.reference = listener.listenToConfiguration(f -> HoconConfigurationLoader.builder().path(f).build(), path);
    this.reference.errors().subscribe(e -> logger.warn(e.getValue().getMessage(), e.getValue()));
    this.configReference = reference.referenceTo(Config.class, NodePath.path(), defaultConfig);
    this.reference.save();

    if (INSTANCE == null) {
      INSTANCE = this;
    }
  }

  public void subscribe(Consumer<Config> subscriber) {
    reference.updates().subscribe(n -> subscriber.accept(config()));
  }

  public void close() {
    reference.close();
  }

  public Config config() {
    Config config = configReference.get();
    return config == null ? defaultConfig : config;
  }

  public static ConfigManager instance() {
    return INSTANCE;
  }
}
