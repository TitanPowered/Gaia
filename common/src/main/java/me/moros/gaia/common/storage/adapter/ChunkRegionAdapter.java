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
import java.util.Locale;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.math.Vector3i;

final class ChunkRegionAdapter implements JsonSerializer<ChunkRegion.Validated>, JsonDeserializer<ChunkRegion.Validated> {
  @Override
  public JsonElement serialize(ChunkRegion.Validated src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject result = new JsonObject();
    result.add("min", context.serialize(src.region().min()));
    result.add("max", context.serialize(src.region().max()));
    result.addProperty("checksum", Long.toHexString(src.checksum()).toUpperCase(Locale.ROOT));
    return result;
  }

  @Override
  public ChunkRegion.Validated deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    var jsonObject = json.getAsJsonObject();
    Vector3i min = context.deserialize(jsonObject.get("min"), Vector3i.class);
    Vector3i max = context.deserialize(jsonObject.get("max"), Vector3i.class);
    long checksum;
    try {
      checksum = Long.parseLong(jsonObject.get("checksum").getAsString(), 16);
    } catch (NumberFormatException e) {
      throw new JsonParseException(e);
    }
    return ChunkRegion.create(Region.of(min, max), checksum);
  }
}
