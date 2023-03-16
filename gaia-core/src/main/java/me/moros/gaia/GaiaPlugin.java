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

package me.moros.gaia;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import me.moros.gaia.api.ArenaPoint;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.config.ConfigManager;
import me.moros.gaia.locale.TranslationManager;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

public interface GaiaPlugin {
  String author();

  String version();

  Logger logger();

  ConfigManager configManager();

  TranslationManager translationManager();

  ArenaManager arenaManager();

  ChunkManager chunkManager();

  BlockState parseBlockData(@Nullable String value);

  @Nullable World findWorld(UUID uid);

  @Nullable GaiaUser findUser(String input);

  Stream<String> users();

  Executor executor();

  @Nullable ArenaPoint pointFromUser(GaiaUser user);

  void teleport(GaiaUser user, UUID worldUid, ArenaPoint point);

  void repeat(Runnable task, long delay, long period);
}
