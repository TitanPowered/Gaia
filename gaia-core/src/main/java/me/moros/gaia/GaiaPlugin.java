package me.moros.gaia;

import java.util.UUID;
import java.util.logging.Logger;

import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaRegion;
import me.moros.gaia.platform.GaiaBlockData;
import me.moros.gaia.platform.GaiaWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface GaiaPlugin {
  @NonNull String getAuthor();

  @NonNull String getVersion();

  @NonNull Logger getLog();

  @NonNull GaiaArenaManager getArenaManager();

  @NonNull GaiaChunk adaptChunk(@NonNull UUID id, @NonNull Arena parent, @NonNull GaiaRegion region);

  @NonNull GaiaBlockData getBlockDataFromString(String value);

  @Nullable GaiaWorld getWorld(UUID uid);
}
