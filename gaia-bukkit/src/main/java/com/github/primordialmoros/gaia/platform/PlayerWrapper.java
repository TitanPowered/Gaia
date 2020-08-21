package com.github.primordialmoros.gaia.platform;

import com.github.primordialmoros.gaia.Gaia;
import com.github.primordialmoros.gaia.api.GaiaVector;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;


public class PlayerWrapper implements GaiaPlayer {
	private final Player player;

	public PlayerWrapper(Player player) {
		this.player = player;
	}

	public Player get() {
		return this.player;
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public boolean isOnline() {
		return player.isOnline();
	}

	@Override
	public GaiaVector getLocation() {
		return GaiaVector.at(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	}

	@Override
	public WorldWrapper getWorld() {
		return new WorldWrapper(player.getWorld());
	}

	@Override
	public String getName() {
		return player.getName();
	}

	@Override
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}

	@Override
	public void sendBrandedMessage(TextComponent text) {
		Gaia.getAudiences().audience(player).sendMessage(TextComponent.ofChildren(Gaia.PREFIX, text));
	}

	@Override
	public void sendMessage(TextComponent text) {
		Gaia.getAudiences().audience(player).sendMessage(text);
	}
}
