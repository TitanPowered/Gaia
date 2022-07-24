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

package me.moros.gaia.util.functional;

import java.util.Objects;

import com.sk89q.worldedit.math.BlockVector3;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaData;
import me.moros.gaia.io.GaiaIO;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AnalyzeOperation extends GaiaOperation {
  private final GaiaData data;

  private AnalyzeOperation(GaiaChunk chunk) {
    super(chunk);
    this.data = new GaiaData(chunk.region().size());
  }

  @Override
  public @Nullable GaiaOperation process(int maxTransactions) {
    BlockVector3 relative, real;
    int counter = 0;
    while (++counter <= maxTransactions && it.hasNext()) {
      relative = it.next();
      real = chunk.region().min().add(relative);
      data.set(relative, world.getBlock(real));
    }
    if (it.hasNext()) {
      return this;
    } else {
      GaiaIO.instance().saveData(chunk, data);
      return null;
    }
  }

  public static @NonNull AnalyzeOperation create(@NonNull GaiaChunk chunk) {
    Objects.requireNonNull(chunk);
    return new AnalyzeOperation(chunk);
  }
}
