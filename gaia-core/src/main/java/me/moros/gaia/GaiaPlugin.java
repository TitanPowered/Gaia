/*
 * Copyright 2020-2021 Moros
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

import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import me.moros.gaia.api.ArenaPoint;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.config.ConfigManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

public interface GaiaPlugin {
  @NonNull String author();

  @NonNull String version();

  @NonNull Logger logger();

  @NonNull ConfigManager configManager();

  @NonNull ArenaManager arenaManager();

  @NonNull ChunkManager chunkManager();

  @NonNull BlockState parseBlockData(@Nullable String value);

  @Nullable World findWorld(@NonNull UUID uid);

  @NonNull Executor executor();

  @Nullable ArenaPoint pointFromUser(@NonNull GaiaUser user);

  void queryCommands(@NonNull String rawQuery, @NonNull GaiaUser recipient);

  void teleport(@NonNull GaiaUser user, @NonNull UUID worldUid, @NonNull ArenaPoint point);

  void reload();
}
