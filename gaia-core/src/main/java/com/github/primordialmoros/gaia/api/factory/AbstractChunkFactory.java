package com.github.primordialmoros.gaia.api.factory;

import com.github.primordialmoros.gaia.api.Arena;
import com.github.primordialmoros.gaia.api.GaiaRegion;

import java.util.UUID;

public interface AbstractChunkFactory<GaiaChunk> {
	GaiaChunk create(UUID id, Arena parent, GaiaRegion region);
}
