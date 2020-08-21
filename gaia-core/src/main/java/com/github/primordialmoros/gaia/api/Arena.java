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

package com.github.primordialmoros.gaia.api;

import com.github.primordialmoros.gaia.platform.GaiaWorld;
import com.github.primordialmoros.gaia.util.Util;
import com.github.primordialmoros.gaia.util.metadata.ArenaMetadata;
import com.github.primordialmoros.gaia.util.metadata.GaiaMetadata;
import com.github.primordialmoros.gaia.util.metadata.Metadatable;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Arena implements Metadatable {
	private final String name;
	private final GaiaWorld world;
	private final GaiaRegion region;
	private final TextComponent info;

	private final List<GaiaChunk> subRegions;

	private boolean reverting;
	private boolean finalized;

	private ArenaMetadata meta;

	public Arena(final String name, final GaiaWorld world, final GaiaRegion region) {
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

	public TextComponent getFormattedName() {
		return TextComponent.of(getName(), NamedTextColor.GOLD);
	}

	public GaiaWorld getWorld() {
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

	public boolean isFinalized() {
		return finalized;
	}

	public void setReverting(boolean value) {
		reverting = value;
	}

	public static TextComponent createInfo(final Arena arena) {
		final int volume = arena.getRegion().getVolume();
		final TextComponent infoDetails = TextComponent.builder("Name: ", NamedTextColor.DARK_AQUA)
			.append(arena.getName(), NamedTextColor.GREEN).append(TextComponent.newline())
			.append("World: ", NamedTextColor.DARK_AQUA)
			.append(arena.getWorld().getName(), NamedTextColor.GREEN).append(TextComponent.newline())
			.append("Dimensions: ", NamedTextColor.DARK_AQUA)
			.append(arena.getDimensions(), NamedTextColor.GREEN).append(TextComponent.newline())
			.append("Volume: ", NamedTextColor.DARK_AQUA)
			.append(String.valueOf(volume), NamedTextColor.GREEN).append(TextComponent.newline())
			.append("Center: ", NamedTextColor.DARK_AQUA)
			.append(arena.getRegion().getCenter().toString(), NamedTextColor.GREEN).append(TextComponent.newline()).append(TextComponent.newline())
			.append("Click to copy center coordinates to clipboard.", NamedTextColor.GRAY).build();

		return TextComponent.builder("> ", NamedTextColor.DARK_GRAY).append(arena.getFormattedName())
			.append(" (" + Util.getSizeDescription(volume) + ")", NamedTextColor.DARK_AQUA)
			.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, infoDetails))
			.clickEvent(ClickEvent.of(ClickEvent.Action.COPY_TO_CLIPBOARD, arena.getRegion().getCenter().toString()))
			.build();
	}

	public String getDimensions() {
		return region.getWidth() + " x " + region.getHeight() + " x " + region.getLength();
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
