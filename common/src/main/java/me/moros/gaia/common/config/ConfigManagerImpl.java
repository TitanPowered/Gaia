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

package me.moros.gaia.common.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import me.moros.gaia.api.config.Config;
import me.moros.gaia.api.config.ConfigManager;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.reference.WatchServiceListener;

public final class ConfigManagerImpl implements ConfigManager {
  private final ConfigImpl defaultConfig;

  private final Logger logger;
  private final WatchServiceListener listener;
  private final ConfigurationReference<CommentedConfigurationNode> reference;
  private final ValueReference<ConfigImpl, CommentedConfigurationNode> configReference;

  public ConfigManagerImpl(Logger logger, Path directory) {
    this.logger = logger;
    this.defaultConfig = new ConfigImpl();
    Path path = directory.resolve("gaia.conf");
    try {
      Files.createDirectories(path.getParent());
      listener = WatchServiceListener.create();
      reference = listener.listenToConfiguration(f -> HoconConfigurationLoader.builder().path(f).build(), path);
      configReference = reference.referenceTo(ConfigImpl.class, NodePath.path(), defaultConfig);
      reference.save();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void subscribe(Consumer<Config> subscriber) {
    reference.updates().subscribe(n -> subscriber.accept(config()));
  }

  @Override
  public void close() {
    try {
      reference.close();
      listener.close();
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  @Override
  public Config config() {
    Config config = configReference.get();
    return config == null ? defaultConfig : config;
  }
}
