/*
 * Copyright 2020-2024 Moros
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
import me.moros.gaia.api.arena.Point;
import me.moros.math.Vector3d;

final class PointAdapter implements JsonSerializer<Point>, JsonDeserializer<Point> {
  @Override
  public JsonElement serialize(Point src, Type typeOfSrc, JsonSerializationContext context) {
    JsonArray array = new JsonArray(5);
    array.add(src.x());
    array.add(src.y());
    array.add(src.z());
    array.add(src.yaw());
    array.add(src.pitch());
    return array;
  }

  @Override
  public Point deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonArray array = json.getAsJsonArray();
    if (array.size() != 5) {
      throw new JsonParseException("Invalid Point: Expected array length 5");
    }
    double x = array.get(0).getAsDouble();
    double y = array.get(1).getAsDouble();
    double z = array.get(2).getAsDouble();
    float yaw = array.get(3).getAsFloat();
    float pitch = array.get(4).getAsFloat();
    return Point.of(Vector3d.of(x, y, z), yaw, pitch);
  }
}
