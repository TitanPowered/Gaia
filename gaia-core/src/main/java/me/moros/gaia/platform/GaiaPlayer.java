package me.moros.gaia.platform;

import me.moros.gaia.api.GaiaVector;

public interface GaiaPlayer extends GaiaSender {
	boolean isOnline();

	GaiaVector getLocation();

	GaiaWorld getWorld();
}
