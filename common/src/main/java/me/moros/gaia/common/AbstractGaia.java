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

package me.moros.gaia.common;

import java.nio.file.Path;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.common.config.ConfigManager;
import me.moros.gaia.common.locale.TranslationManager;
import org.slf4j.Logger;

public abstract class AbstractGaia<T> {
  protected final T parent;
  private final Path path;
  private final Logger logger;

  private final TranslationManager translationManager;

  protected final GaiaFactory factory;
  private GaiaImpl gaia;

  protected AbstractGaia(T parent, Path path, Logger logger) {
    this.parent = parent;
    this.path = path;
    this.logger = logger;

    ConfigManager.createInstance(logger, this.path);
    this.translationManager = new TranslationManager(logger, this.path);
    this.factory = new GaiaFactory();
  }

  protected void load() {
    this.gaia = new GaiaImpl(this, factory);
    long startTime = System.currentTimeMillis();
    gaia.storage().loadAllArenas().thenAccept(arenas -> {
      arenas.forEach(gaia.arenaService()::add);
      long delta = System.currentTimeMillis() - startTime;
      int size = gaia.arenaService().size();
      logger.info(String.format("Successfully loaded %d %s (%dms)", size, (size == 1 ? "arena" : "arenas"), delta));
    });
  }

  protected void disable() {
    ConfigManager.instance().close();
    translationManager.close();
    gaia.shutdown();
  }

  protected Path path() {
    return path;
  }

  protected Logger logger() {
    return logger;
  }

  protected Gaia api() {
    return gaia;
  }
}
