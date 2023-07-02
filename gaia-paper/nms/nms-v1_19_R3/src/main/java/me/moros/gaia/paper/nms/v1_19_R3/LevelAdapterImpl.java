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

package me.moros.gaia.paper.nms.v1_19_R3;

import me.moros.gaia.api.platform.Level;
import me.moros.gaia.common.platform.LevelAdapter;
import org.bukkit.World;

public class LevelAdapterImpl implements LevelAdapter<World> {
  @Override
  public Level asNative(World world) {
    return new NativeLevel(world);
  }
}