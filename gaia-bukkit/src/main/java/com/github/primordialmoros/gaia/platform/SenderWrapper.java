package com.github.primordialmoros.gaia.platform;

import com.github.primordialmoros.gaia.Gaia;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

public class SenderWrapper implements GaiaSender {
	private final CommandSender sender;

	public SenderWrapper(CommandSender sender) {
		this.sender = sender;
	}

	public CommandSender get() {
		return this.sender;
	}

	@Override
	public String getName() {
		return sender.getName();
	}

	@Override
	public boolean hasPermission(String permission) {
		return sender.hasPermission(permission);
	}

	@Override
	public void sendBrandedMessage(TextComponent text) {
		Gaia.getAudiences().audience(sender).sendMessage(TextComponent.ofChildren(Gaia.PREFIX, text));
	}

	@Override
	public void sendMessage(TextComponent text) {
		Gaia.getAudiences().audience(sender).sendMessage(text);
	}
}
