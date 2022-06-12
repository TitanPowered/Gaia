/*
 * Copyright 2020-2021 Moros
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

package me.moros.gaia.io;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import me.moros.gaia.api.ArenaPoint;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GaiaAdapter {
  public static final class GaiaBlockVectorAdapter implements JsonSerializer<BlockVector3>, JsonDeserializer<BlockVector3> {
    @Override
    public @NonNull JsonElement serialize(@NonNull BlockVector3 src, @NonNull Type typeOfSrc, @NonNull JsonSerializationContext context) {
      JsonArray array = new JsonArray();
      array.add(src.getX());
      array.add(src.getY());
      array.add(src.getZ());
      return array;
    }

    @Override
    public @NonNull BlockVector3 deserialize(@NonNull JsonElement json, @NonNull Type typeOfT, @NonNull JsonDeserializationContext context) throws JsonParseException {
      JsonArray array = json.getAsJsonArray();
      if (array.size() != 3) {
        throw new JsonParseException("Invalid Vector: Expected array length 3");
      }
      BlockVector3 v = BlockVector3.at(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
      if (!BlockVector3.isLongPackable(v)) {
        throw new JsonParseException("Invalid Vector: " + json);
      }
      return v;
    }
  }
  public static final class GaiaPointAdapter implements JsonSerializer<ArenaPoint>, JsonDeserializer<ArenaPoint> {
    @Override
    public @NonNull JsonElement serialize(@NonNull ArenaPoint src, @NonNull Type typeOfSrc, @NonNull JsonSerializationContext context) {
      JsonArray array = new JsonArray();
      array.add(src.v().getX());
      array.add(src.v().getY());
      array.add(src.v().getZ());
      array.add(src.yaw());
      array.add(src.pitch());
      return array;
    }

    @Override
    public @NonNull ArenaPoint deserialize(@NonNull JsonElement json, @NonNull Type typeOfT, @NonNull JsonDeserializationContext context) throws JsonParseException {
      JsonArray array = json.getAsJsonArray();
      if (array.size() != 5) {
        throw new JsonParseException("Invalid Point: Expected array length 5");
      }
      double x = array.get(0).getAsDouble();
      double y = array.get(1).getAsDouble();
      double z = array.get(2).getAsDouble();
      float yaw = array.get(3).getAsFloat();
      float pitch = array.get(4).getAsFloat();
      return new ArenaPoint(Vector3.at(x, y, z), yaw, pitch);
    }
  }
}
