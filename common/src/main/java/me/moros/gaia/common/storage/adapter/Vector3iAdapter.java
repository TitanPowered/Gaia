/*
 * Copyright 2020-2023 Moros
 *
 * This file is part of Gaia.
 *
 * Gaia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gaia is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gaia. If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.gaia.common.storage.adapter;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import me.moros.gaia.api.util.ChunkUtil;
import me.moros.math.Vector3i;

final class Vector3iAdapter implements JsonSerializer<Vector3i>, JsonDeserializer<Vector3i> {
  @Override
  public JsonElement serialize(Vector3i src, Type typeOfSrc, JsonSerializationContext context) {
    JsonArray array = new JsonArray(3);
    array.add(src.blockX());
    array.add(src.blockY());
    array.add(src.blockZ());
    return array;
  }

  @Override
  public Vector3i deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonArray array = json.getAsJsonArray();
    if (array.size() != 3) {
      throw new JsonParseException("Invalid Vector: Expected array length 3");
    }
    Vector3i v = Vector3i.of(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
    if (!ChunkUtil.isValidPosition(v)) {
      throw new JsonParseException("Invalid Vector: " + json);
    }
    return v;
  }
}
