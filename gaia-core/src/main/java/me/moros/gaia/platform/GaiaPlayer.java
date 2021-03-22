package me.moros.gaia.platform;

import me.moros.gaia.api.GaiaVector;

public interface GaiaPlayer extends GaiaUser {
	boolean isOnline();

	GaiaVector getLocation();

	GaiaWorld getWorld();
}
