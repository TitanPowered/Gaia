package com.github.primordialmoros.gaia;

import com.github.primordialmoros.gaia.api.GaiaChunk;
import com.github.primordialmoros.gaia.api.factory.AbstractChunkFactory;
import com.github.primordialmoros.gaia.platform.GaiaBlockData;
import com.github.primordialmoros.gaia.platform.GaiaWorld;

import java.util.UUID;
import java.util.logging.Logger;

public interface GaiaPlugin {
	String getAuthor();

	String getVersion();

	Logger getLog();

	AbstractArenaManager getArenaManager();

	AbstractChunkFactory<GaiaChunk> getChunkFactory();

	GaiaBlockData getBlockDataFromString(String value);

	GaiaWorld getWorld(UUID uid);
}
