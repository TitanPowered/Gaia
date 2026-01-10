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

import me.moros.gaia.api.util.ChunkUtil;
import me.moros.math.Vector3i;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

final class Vector3iSerializer implements TypeSerializer<Vector3i> {
  static final Vector3iSerializer INSTANCE = new Vector3iSerializer();

  private Vector3iSerializer() {
  }

  @Override
  public Vector3i deserialize(Type type, ConfigurationNode source) throws SerializationException {
    int[] array = source.get(int[].class, new int[0]);
    if (array.length != 3) {
      throw new SerializationException("Invalid Vector: Expected array length 3");
    }
    Vector3i result = Vector3i.from(array);
    if (!ChunkUtil.isValidPosition(result)) {
      throw new SerializationException("Invalid Vector %s exceeds bounds!".formatted(result));
    }
    return result;
  }

  @Override
  public void serialize(Type type, @Nullable Vector3i vector, ConfigurationNode target) throws SerializationException {
    if (vector == null) {
      target.raw(null);
      return;
    }
    target.set(vector.toIntArray());
  }
}
