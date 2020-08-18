/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *
 * 	  This file is part of Gaia.
 *
 *    Gaia is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Gaia is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Gaia.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.primordialmoros.gaia;

import com.github.primordialmoros.gaia.util.GaiaChunk;
import com.github.primordialmoros.gaia.util.GaiaRegion;
import com.github.primordialmoros.gaia.util.Util;
import com.github.primordialmoros.gaia.util.metadata.ArenaMetadata;
import com.github.primordialmoros.gaia.util.metadata.GaiaMetadata;
import com.github.primordialmoros.gaia.util.metadata.Metadatable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Arena implements Metadatable {

	private final String name;
	private final World world;
	private final GaiaRegion region;
	private final TextComponent info;

	private final List<GaiaChunk> subRegions;

	private boolean reverting;
	private boolean finalized;

	private ArenaMetadata meta;

	public Arena(final String name, final World world, final GaiaRegion region) {
		this.world = world;
		this.name = name.toLowerCase();
		this.region = region;
		info = createInfo(this);
		subRegions = new ArrayList<>();
		reverting = false;
		finalized = false;
	}

	public void addSubRegion(final GaiaChunk chunk) {
		subRegions.add(chunk);
	}

	public boolean finalizeArena() {
		if (subRegions.isEmpty() || finalized) return false;
		subRegions.sort(GaiaChunk.ZX_ORDER);
		finalized = true;
		return true;
	}

	public List<GaiaChunk> getSubRegions() {
		return subRegions;
	}

	public String getName() {
		return name;
	}

	public String getFormattedName() {
		return ChatColor.GOLD + getName();
	}

	public World getWorld() {
		return world;
	}

	public UUID getWorldUID() {
		return world.getUID();
	}

	public GaiaRegion getRegion() {
		return region;
	}

	public TextComponent getInfo() {
		return info;
	}

	public boolean isReverting() {
		return reverting;
	}

	/**
	 * Attempt to start or stop the reverting of the arena
	 *
	 * @param value whether it should start reverting or not
	 * @return false if arena hasn't been fully analyzed or is already in the desired state, true otherwise
	 */
	public boolean setReverting(boolean value) {
		if (!finalized || reverting == value) return false;
		reverting = value;
		return true;
	}

	public static TextComponent createInfo(final Arena arena) {
		final int volume = arena.getRegion().getVolume();
		final TextComponent createdInfo = new TextComponent(ChatColor.DARK_GRAY + "> " + arena.getFormattedName() + ChatColor.DARK_AQUA + " (" + Util.getSizeDescription(volume) + ")");
		final String infoDetails = ChatColor.DARK_AQUA + "Name: " + ChatColor.GREEN + arena.getName() + "\n" +
			ChatColor.DARK_AQUA + "World: " + ChatColor.GREEN + arena.getWorld().getName() + "\n" +
			ChatColor.DARK_AQUA + "Dimensions: " + ChatColor.GREEN + arena.getRegion().getWidth() + " x " + arena.getRegion().getHeight() + " x " + arena.getRegion().getLength() + "\n" +
			ChatColor.DARK_AQUA + "Volume: " + ChatColor.GREEN + volume + " blocks" + "\n" +
			ChatColor.DARK_AQUA + "Center: " + ChatColor.GREEN + arena.getRegion().getCenter().toString() + "\n\n" +
			ChatColor.GRAY + "Click to copy center coordinates to clipboard.";

		createdInfo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(infoDetails)));
		createdInfo.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, arena.getRegion().getCenter().toString()));
		return createdInfo;
	}

	@Override
	public GaiaMetadata getMetadata() {
		return meta;
	}

	@Override
	public void setMetadata(GaiaMetadata meta) {
		this.meta = (ArenaMetadata) meta;
	}
}
