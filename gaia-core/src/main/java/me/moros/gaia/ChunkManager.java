/*
 *   Copyright 2020-2021 Moros <https://github.com/PrimordialMoros>
 *
 *    This file is part of Gaia.
 *
 *    Gaia is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Gaia is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Gaia.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.gaia;

import com.sk89q.worldedit.world.World;
import me.moros.gaia.api.GaiaChunk;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ChunkManager {
  void shutdown();

  int getRemainingTasks();

  void cancelTasks(@NonNull GaiaChunk chunk);

  void revertChunk(@NonNull GaiaChunk chunk, @NonNull World world);

  void analyzeChunk(@NonNull GaiaChunk chunk, @NonNull World world);
}
