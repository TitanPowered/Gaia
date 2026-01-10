/*
 * Copyright 2020-2026 Moros
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

package me.moros.gaia.common.storage.serializer;

import java.lang.reflect.Type;

import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.util.ChunkUtil;
import me.moros.math.Vector3d;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

final class PointSerializer implements TypeSerializer<Point> {
  static final PointSerializer INSTANCE = new PointSerializer();

  private PointSerializer() {
  }

  @Override
  public Point deserialize(Type type, ConfigurationNode source) throws SerializationException {
    double[] array = source.get(double[].class, new double[0]);
    if (array.length != 5) {
      throw new SerializationException("Invalid Point: Expected array length 5");
    }
    Vector3d pos = Vector3d.of(array[0], array[1], array[2]);
    if (!ChunkUtil.isValidPosition(pos)) {
      throw new SerializationException("Invalid Vector %s exceeds bounds!".formatted(pos));
    }
    float yaw = (float) array[3];
    float pitch = (float) array[4];
    return Point.of(pos, normalizeYaw(yaw), normalizePitch(pitch));
  }

  @Override
  public void serialize(Type type, @Nullable Point point, ConfigurationNode target) throws SerializationException {
    if (point == null) {
      target.raw(null);
      return;
    }
    target.set(new double[]{point.x(), point.y(), point.z(), normalizeYaw(point.yaw()), normalizePitch(point.pitch())});
  }

  private static float normalizeYaw(float yaw) {
    yaw %= 360;
    if (yaw >= 180) {
      yaw -= 360;
    } else if (yaw < -180) {
      yaw += 360;
    }
    return yaw;
  }

  private static float normalizePitch(float pitch) {
    return Math.clamp(pitch, -90, 90);
  }
}
