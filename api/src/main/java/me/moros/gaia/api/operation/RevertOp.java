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

package me.moros.gaia.api.operation;

import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.api.platform.Level;
import me.moros.gaia.api.util.ChunkUtil;
import me.moros.math.FastMath;

final class RevertOp extends AbstractOp.LevelChunkOp<Void> implements GaiaOperation.Revert {
  private Snapshot snapshot;
  private final int amount;

  RevertOp(Level level, Snapshot snapshot, int sectionsPerTick) {
    super(level, snapshot.chunk());
    this.snapshot = snapshot;
    this.amount = FastMath.clamp(sectionsPerTick, 16, snapshot.sections()) * ChunkUtil.CHUNK_SECTION_VOLUME;
  }

  @Override
  protected Result processStep() {
    if (level.restoreSnapshot(snapshot, amount)) {
      return Result.CONTINUE;
    } else {
      this.snapshot = null; // Help gc
      future.complete(null);
      return Result.REMOVE;
    }
  }
}
