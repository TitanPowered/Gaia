/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *
 * 	  This file is part of Gaia.
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

package com.github.primordialmoros.gaia.platform;

import org.bukkit.block.Block;

public class BlockWrapper implements GaiaBlock {
	private final Block block;

	public BlockWrapper(Block block) {
		this.block = block;
	}

	@Override
	public BlockDataWrapper getBlockData() {
		return new BlockDataWrapper(block.getBlockData());
	}

	@Override
	public void setBlockData(GaiaBlockData data) {
		block.setBlockData(((BlockDataWrapper) data).get());
	}
}
