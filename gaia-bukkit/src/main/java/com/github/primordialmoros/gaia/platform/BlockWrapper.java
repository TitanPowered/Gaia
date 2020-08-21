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
