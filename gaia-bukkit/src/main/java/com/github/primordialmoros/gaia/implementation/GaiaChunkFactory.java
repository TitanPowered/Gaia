package com.github.primordialmoros.gaia.implementation;

import com.github.primordialmoros.gaia.api.Arena;
import com.github.primordialmoros.gaia.api.GaiaChunk;
import com.github.primordialmoros.gaia.api.GaiaRegion;
import com.github.primordialmoros.gaia.api.factory.AbstractChunkFactory;

import java.util.UUID;

public class GaiaChunkFactory implements AbstractChunkFactory<GaiaChunk> {
	@Override
	public PaperGaiaChunk create(UUID id, Arena parent, GaiaRegion region) {
		return new PaperGaiaChunk(id, parent, region);
	}
}
