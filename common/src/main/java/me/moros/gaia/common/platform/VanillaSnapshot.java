/*
 * Copyright 2020-2025 Moros
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
import me.moros.gaia.common.platform.codec.Codecs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

record VanillaSnapshot(ChunkRegion chunk, Section[] sectionData) implements Snapshot {
  @Override
  public String getStateString(int x, int y, int z) {
    return Codecs.blockStateCodec().toString(sectionData[ChunkUtil.toChunkPos(y)].state(x, y, z));
  }

  @Override
  public int sections() {
    return sectionData.length;
  }

  @FunctionalInterface
  interface Section {
    BlockState state(int x, int y, int z);
  }

  private static final Section EMPTY_SECTION = (x, y, z) -> Blocks.AIR.defaultBlockState();

  private record SectionWrapper(PalettedContainer<BlockState> palettedContainer) implements Section {
    @Override
    public BlockState state(int x, int y, int z) {
      return palettedContainer.get(x & 0xF, y & 0xF, z & 0xF);
    }
  }

  static VanillaSnapshot from(ChunkRegion chunk, ChunkAccess access) {
    int size = ChunkUtil.calculateSections(chunk.region());
    LevelChunkSection[] cs = access.getSections();
    int sectionIndexOffset = access.getSectionIndex(chunk.region().min().blockY());
    Section[] sections = new Section[size];
    for (int i = 0; i < sections.length; i++) {
      sections[i] = copySection(cs[sectionIndexOffset + i]);
    }
    return new VanillaSnapshot(chunk, sections);
  }

  private static Section copySection(LevelChunkSection section) {
    return section.hasOnlyAir() ? EMPTY_SECTION : new SectionWrapper(section.getStates().copy());
  }
}
