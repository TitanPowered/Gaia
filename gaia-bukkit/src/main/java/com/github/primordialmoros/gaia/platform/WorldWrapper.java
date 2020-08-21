package com.github.primordialmoros.gaia.platform;

import com.github.primordialmoros.gaia.api.GaiaVector;
import org.bukkit.World;

import java.util.UUID;

public class WorldWrapper implements GaiaWorld {
	private final World world;

	public WorldWrapper(World world) {
		this.world = world;
	}

	public World get() {
		return this.world;
	}

	@Override
	public GaiaBlock getBlockAt(GaiaVector v) {
		return new BlockWrapper(world.getBlockAt(v.getX(), v.getY(), v.getZ()));
	}

	@Override
	public String getName() {
		return world.getName();
	}

	@Override
	public UUID getUID() {
		return world.getUID();
	}
}
