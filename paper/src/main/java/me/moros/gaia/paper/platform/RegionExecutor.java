/*
 * Copyright 2020-2026 Moros
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

package me.moros.gaia.paper.platform;

import java.util.concurrent.Executor;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public sealed interface RegionExecutor extends Executor permits PaperRegionExecutor {
  default void execute(World world, int x, int z, Runnable task) {
    execute(task);
  }

  static RegionExecutor create(Plugin plugin) {
    if (Folia.FOLIA) {
      return new FoliaRegionExecutor(plugin);
    } else {
      return new PaperRegionExecutor(plugin);
    }
  }
}
