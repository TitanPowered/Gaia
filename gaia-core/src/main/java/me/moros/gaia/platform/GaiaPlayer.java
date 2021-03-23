package me.moros.gaia.platform;

import me.moros.gaia.api.GaiaVector;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface GaiaPlayer extends GaiaUser {
	boolean isOnline();

	@NonNull GaiaVector getLocation();

	@NonNull GaiaWorld getWorld();
}
