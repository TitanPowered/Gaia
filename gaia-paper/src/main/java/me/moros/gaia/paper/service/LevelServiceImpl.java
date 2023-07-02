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

package me.moros.gaia.paper.service;

import me.moros.gaia.api.platform.Level;
import me.moros.gaia.api.service.LevelService;
import me.moros.gaia.common.platform.LevelAdapter;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

public record LevelServiceImpl(Logger logger, LevelAdapter<World> nativeProvider) implements LevelService {
  @Override
  public @Nullable Level findLevel(Key level) {
    var world = Bukkit.getServer().getWorld(new NamespacedKey(level.namespace(), level.value()));
    if (world == null) {
      logger().warn("Couldn't find level with key " + level);
      return null;
    }
    return nativeProvider().asNative(world);
  }
}
