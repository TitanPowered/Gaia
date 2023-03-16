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

package me.moros.gaia.util.functional;

import java.util.Objects;

import com.sk89q.worldedit.math.BlockVector3;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaData;
import me.moros.gaia.io.GaiaIO;

public class AnalyzeOperation extends GaiaOperation {
  private final GaiaData data;

  private AnalyzeOperation(GaiaChunk chunk) {
    super(chunk);
    this.data = new GaiaData(chunk.region().size());
  }

  @Override
  public void process(BlockVector3 relative) {
    data.set(relative, world.getBlock(chunk.region().min().add(relative)));
  }

  @Override
  public void onFinish() {
    GaiaIO.instance().saveData(chunk, data);
  }

  public static AnalyzeOperation create(GaiaChunk chunk) {
    Objects.requireNonNull(chunk);
    return new AnalyzeOperation(chunk);
  }
}
