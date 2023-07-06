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

package me.moros.gaia.common.platform;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.api.util.ChunkUtil;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

record VanillaSnapshot(ChunkRegion chunk, VanillaSection[] sectionData) implements Snapshot {
  @Override
  public String getStateString(int x, int y, int z) {
    return sectionData[ChunkUtil.toChunkPos(y)].state(x, y, z).toString();
  }

  @Override
  public int sections() {
    return sectionData.length;
  }

  static Snapshot from(ChunkRegion chunk, ChunkAccess access) {
    int size = ChunkUtil.calculateSections(chunk.region());
    LevelChunkSection[] cs = access.getSections();
    int sectionIndexOffset = access.getSectionIndex(chunk.region().min().blockY());
    VanillaSection[] sections = new VanillaSection[size];
    for (int i = 0; i < sections.length; i++) {
      sections[i] = VanillaSection.from(cs[sectionIndexOffset + i]);
    }
    return new VanillaSnapshot(chunk, sections);
  }
}
