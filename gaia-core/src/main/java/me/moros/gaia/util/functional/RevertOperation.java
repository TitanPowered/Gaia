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

package me.moros.gaia.util.functional;

import java.util.Objects;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RevertOperation extends GaiaOperation {
  private final GaiaData data;

  private RevertOperation(GaiaChunk chunk, GaiaData data) {
    super(chunk);
    this.data = data;
  }

  @Override
  public @Nullable GaiaOperation process(int maxTransactions) {
    BlockVector3 relative, real;
    int counter = 0;
    while (++counter <= maxTransactions && it.hasNext()) {
      relative = it.next();
      real = chunk.region().min().add(relative);
      try {
        world.setBlock(real, data.get(relative), SideEffectSet.none());
      } catch (Exception e) {
        // Ignore
      }
    }
    if (it.hasNext()) {
      return this;
    } else {
      chunk.cancelReverting();
      return null;
    }
  }

  public static @NonNull RevertOperation create(@NonNull GaiaChunk chunk, @NonNull GaiaData data) {
    Objects.requireNonNull(chunk);
    Objects.requireNonNull(data);
    return new RevertOperation(chunk, data);
  }
}
