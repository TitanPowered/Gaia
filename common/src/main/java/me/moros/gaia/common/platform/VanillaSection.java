/*
 * Copyright 2020-2024 Moros
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

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

@FunctionalInterface
interface VanillaSection {
  BlockState state(int x, int y, int z);

  record VanillaSectionWrapper(PalettedContainer<BlockState> palettedContainer) implements VanillaSection {
    private static final VanillaSection EMPTY = (x, y, z) -> Blocks.AIR.defaultBlockState();

    @Override
    public BlockState state(int x, int y, int z) {
      return palettedContainer.get(x & 0xF, y & 0xF, z & 0xF);
    }
  }

  static VanillaSection from(LevelChunkSection section) {
    return section.hasOnlyAir() ? VanillaSectionWrapper.EMPTY : new VanillaSectionWrapper(section.getStates().copy());
  }
}
