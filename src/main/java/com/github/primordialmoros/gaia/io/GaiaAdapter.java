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

package com.github.primordialmoros.gaia.io;

import com.github.primordialmoros.gaia.util.GaiaVector;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class GaiaAdapter implements JsonSerializer<GaiaVector>, JsonDeserializer<GaiaVector> {

	@Override
	public JsonElement serialize(GaiaVector src, Type typeOfSrc, JsonSerializationContext context) {
		JsonArray array = new JsonArray(3);
		array.add(src.getX());
		array.add(src.getY());
		array.add(src.getZ());
		return array;
	}

	@Override
	public GaiaVector deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonArray array = json.getAsJsonArray();
		if (array.size() != 3) throw new JsonParseException("Invalid GaiaVector: Expected array length 3");
		GaiaVector v = GaiaVector.at(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
		if (!GaiaVector.isValidVector(v)) throw new JsonParseException("Invalid GaiaVector: " + json.toString());
		return v;
	}
}
