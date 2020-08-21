/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *   Copyright (C) sk89q <http://www.sk89q.com>
 *   Copyright (C) WorldEdit team and contributors
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

package com.github.primordialmoros.gaia.io;

import com.github.primordialmoros.gaia.GaiaPlugin;
import com.github.primordialmoros.gaia.api.GaiaData;
import com.github.primordialmoros.gaia.api.GaiaVector;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GaiaReader implements Closeable {
	private final GaiaPlugin platform;
	private final NBTInputStream inputStream;

	/**
	 * Create a new instance.
	 *
	 * @param inputStream the input stream to read from
	 */
	protected GaiaReader(GaiaPlugin platform, NBTInputStream inputStream) {
		this.platform = platform;
		this.inputStream = inputStream;
	}

	private CompoundTag getBaseTag() throws IOException {
		return (CompoundTag) inputStream.readNamedTag().getTag();
	}

	protected GaiaData read() throws IOException {
		CompoundTag schematicTag = getBaseTag();
		Map<String, Tag> schematic = schematicTag.getValue();

		int width = requireTag(schematic, "Width", ShortTag.class).getValue();
		int height = requireTag(schematic, "Height", ShortTag.class).getValue();
		int length = requireTag(schematic, "Length", ShortTag.class).getValue();

		IntTag paletteMaxTag = getTag(schematic, "PaletteMax", IntTag.class);
		Map<String, Tag> paletteObject = requireTag(schematic, "Palette", CompoundTag.class).getValue();
		if (paletteMaxTag != null && paletteObject.size() != paletteMaxTag.getValue()) {
			throw new IOException("Block palette size does not match expected size.");
		}

		Map<Integer, String> palette = new HashMap<>();

		for (String palettePart : paletteObject.keySet()) {
			int id = requireTag(paletteObject, palettePart, IntTag.class).getValue();
			palette.put(id, palettePart);
		}

		byte[] blocks = requireTag(schematic, "BlockData", ByteArrayTag.class).getValue();
		int index = 0;
		int i = 0;
		int value;
		int varintLength;
		GaiaData data = new GaiaData(GaiaVector.at(width, height, length));
		while (i < blocks.length) {
			value = 0;
			varintLength = 0;

			while (true) {
				value |= (blocks[i] & 127) << (varintLength++ * 7);
				if (varintLength > 5) {
					throw new IOException("VarInt too big (probably corrupted data)");
				}
				if ((blocks[i] & 128) != 128) {
					i++;
					break;
				}
				i++;
			}
			// index = (y * length * width) + (z * width) + x
			int y = index / (width * length);
			int z = (index % (width * length)) / width;
			int x = (index % (width * length)) % width;
			data.setDataAt(x, y, z, platform.getBlockDataFromString(palette.get(value)));
			index++;
		}
		return data;
	}

	protected static <T extends Tag> T requireTag(Map<String, Tag> items, String key, Class<T> expected) throws IOException {
		if (!items.containsKey(key)) {
			throw new IOException("Schematic file is missing a \"" + key + "\" tag");
		}
		Tag tag = items.get(key);
		if (!expected.isInstance(tag)) {
			throw new IOException(key + " tag is not of tag type " + expected.getName());
		}
		return expected.cast(tag);
	}

	@Nullable
	protected static <T extends Tag> T getTag(Map<String, Tag> items, String key, Class<T> expected) {
		if (!items.containsKey(key)) {
			return null;
		}
		Tag test = items.get(key);
		if (!expected.isInstance(test)) {
			return null;
		}
		return expected.cast(test);
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
	}
}
