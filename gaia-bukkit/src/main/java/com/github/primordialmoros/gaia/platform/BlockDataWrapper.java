package com.github.primordialmoros.gaia.platform;

import org.bukkit.block.data.BlockData;

public class BlockDataWrapper implements GaiaBlockData {
	private final BlockData blockData;

	public BlockDataWrapper(BlockData blockData) {
		this.blockData = blockData;
	}

	public BlockData get() {
		return blockData;
	}

	@Override
	public String getAsString() {
		return blockData.getAsString();
	}
}
