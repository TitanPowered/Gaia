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

package me.moros.gaia.fabric.service;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.platform.Level;
import me.moros.gaia.api.service.LevelService;
import me.moros.gaia.fabric.platform.FabricLevel;
import net.kyori.adventure.key.Key;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.checkerframework.checker.nullness.qual.Nullable;

public record LevelServiceImpl(Gaia plugin, MinecraftServer server) implements LevelService {
  @Override
  public @Nullable Level findLevel(Key level) {
    var world = match(level);
    if (world == null) {
      plugin().logger().warn("Couldn't find level with key " + level);
      return null;
    }
    return new FabricLevel(world);
  }

  private @Nullable ServerLevel match(Key worldKey) {
    for (ServerLevel level : server.getAllLevels()) {
      if (worldKey.equals(level.dimension().location())) {
        return level;
      }
    }
    return null;
  }
}
