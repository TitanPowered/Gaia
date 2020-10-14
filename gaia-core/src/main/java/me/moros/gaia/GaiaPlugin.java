package me.moros.gaia;

import co.aikar.commands.CommandIssuer;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.factory.AbstractChunkFactory;
import me.moros.gaia.platform.GaiaBlockData;
import me.moros.gaia.platform.GaiaWorld;
import net.kyori.adventure.audience.Audience;

import java.util.UUID;
import java.util.logging.Logger;

public interface GaiaPlugin {
	String getAuthor();

	String getVersion();

	Logger getLog();

	Audience getAudience(CommandIssuer issuer);

	GaiaArenaManager getArenaManager();

	AbstractChunkFactory<GaiaChunk> getChunkFactory();

	GaiaBlockData getBlockDataFromString(String value);

	GaiaWorld getWorld(UUID uid);
}
