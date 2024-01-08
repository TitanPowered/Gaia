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
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.math.Vector3i;
import net.kyori.adventure.key.Key;

@SuppressWarnings("PatternValidation")
final class ArenaAdapter implements JsonSerializer<Arena>, JsonDeserializer<Arena> {
  private static final int VERSION = 2;

  private static final Type CHUNK_LIST = TypeToken.getParameterized(List.class, ChunkRegion.Validated.class).getType();
  private static final Type POINT_LIST = TypeToken.getParameterized(List.class, Point.class).getType();

  @Override
  public JsonElement serialize(Arena src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject result = new JsonObject();
    result.addProperty("version", VERSION);
    result.addProperty("name", src.name());
    result.addProperty("level", src.level().asString());
    result.add("min", context.serialize(src.region().min()));
    result.add("max", context.serialize(src.region().max()));
    result.addProperty("amount", src.chunks().size());
    result.add("chunks", context.serialize(src.chunks()));
    result.add("points", context.serialize(src.points()));
    return result;
  }

  @Override
  public Arena deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    var jsonObject = json.getAsJsonObject();
    int version = jsonObject.get("version").getAsInt();
    if (version != VERSION) {
      throw new JsonParseException(String.format("Unknown data version, expected %d but found %d", VERSION, version));
    }
    String name = jsonObject.get("name").getAsString();
    Key level = Key.key(jsonObject.get("level").getAsString());
    Vector3i min = context.deserialize(jsonObject.get("min"), Vector3i.class);
    Vector3i max = context.deserialize(jsonObject.get("max"), Vector3i.class);
    var region = Region.of(min, max);
    int amount = jsonObject.get("amount").getAsInt();
    List<ChunkRegion.Validated> chunks = context.deserialize(jsonObject.get("chunks"), CHUNK_LIST);
    if (amount != chunks.size()) {
      throw new JsonParseException(String.format("Unable to parse arena, expected %d chunk regions but found %d", amount, chunks.size()));
    }
    List<Point> points = context.deserialize(jsonObject.get("points"), POINT_LIST);
    return Arena.builder().name(name).level(level).region(region).chunks(chunks).points(points).build();
  }
}
