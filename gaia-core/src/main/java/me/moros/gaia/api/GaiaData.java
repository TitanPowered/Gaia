/*
 * Copyright 2020-2022 Moros
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

package me.moros.gaia.api;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class GaiaData {
  private final BlockState[][][] data;
  private final BlockVector3 size;

  public GaiaData(@NonNull BlockVector3 size) {
    this.data = new BlockState[size.getX()][size.getY()][size.getZ()];
    this.size = size;
  }

  public @NonNull BlockState get(@NonNull BlockVector3 v) {
    return get(v.getX(), v.getY(), v.getZ());
  }

  public @NonNull BlockState get(int x, int y, int z) {
    return data[x][y][z];
  }

  public void set(@NonNull BlockVector3 v, @NonNull BlockState gaiaBlockData) {
    set(v.getX(), v.getY(), v.getZ(), gaiaBlockData);
  }

  public void set(int x, int y, int z, @NonNull BlockState gaiaBlockData) {
    data[x][y][z] = gaiaBlockData;
  }

  public @NonNull BlockVector3 size() {
    return size;
  }
}
