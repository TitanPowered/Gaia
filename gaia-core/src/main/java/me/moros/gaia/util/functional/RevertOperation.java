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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaData;
import me.moros.gaia.event.ChunkRevertEvent;

public class RevertOperation extends GaiaOperation {
  private final GaiaData data;

  private RevertOperation(GaiaChunk chunk, GaiaData data) {
    super(chunk);
    this.data = data;
  }

  @Override
  public void process(BlockVector3 relative) {
    try {
      world.setBlock(chunk.region().min().add(relative), data.get(relative), SideEffectSet.none());
    } catch (Exception ignore) {
    }
  }

  @Override
  public void onFinish() {
    chunk.cancelReverting();
    long delta = System.currentTimeMillis() - startTime;
    WorldEdit.getInstance().getEventBus().post(new ChunkRevertEvent(chunk, delta));
  }

  public static RevertOperation create(GaiaChunk chunk, GaiaData data) {
    Objects.requireNonNull(chunk);
    Objects.requireNonNull(data);
    return new RevertOperation(chunk, data);
  }
}
