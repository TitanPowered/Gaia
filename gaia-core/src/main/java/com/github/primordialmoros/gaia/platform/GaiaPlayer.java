package com.github.primordialmoros.gaia.platform;

import com.github.primordialmoros.gaia.api.GaiaVector;

public interface GaiaPlayer extends GaiaSender {
	boolean isOnline();

	GaiaVector getLocation();

	GaiaWorld getWorld();
}
