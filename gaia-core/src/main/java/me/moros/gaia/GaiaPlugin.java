package me.moros.gaia;

import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.factory.AbstractChunkFactory;
import me.moros.gaia.platform.GaiaBlockData;
import me.moros.gaia.platform.GaiaWorld;

import java.util.UUID;
import java.util.logging.Logger;

public interface GaiaPlugin {
	String getAuthor();

	String getVersion();

	Logger getLog();

	GaiaArenaManager getArenaManager();

	AbstractChunkFactory<GaiaChunk> getChunkFactory();

	GaiaBlockData getBlockDataFromString(String value);

	GaiaWorld getWorld(UUID uid);
}
