/*
 * Copyright 2020-2025 Moros
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
import java.util.Locale;

import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.math.Vector3i;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

final class ChunkRegionSerializer extends AbstractSerializer<ChunkRegion.Validated> {
  static final ChunkRegionSerializer INSTANCE = new ChunkRegionSerializer();

  private static final String MIN = "min";
  private static final String MAX = "max";
  private static final String CHECKSUM = "checksum";

  private ChunkRegionSerializer() {
  }

  @Override
  public ChunkRegion.Validated deserialize(Type type, ConfigurationNode source) throws SerializationException {
    Vector3i min = nonVirtualNode(source, MIN).get(Vector3i.class);
    Vector3i max = nonVirtualNode(source, MAX).get(Vector3i.class);
    if (min == null || max == null) {
      throw new SerializationException("A min and max are required to deserialize a ChunkRegion");
    }
    long checksum;
    try {
      checksum = Long.parseLong(nonVirtualNode(source, CHECKSUM).getString(""), 16);
    } catch (NumberFormatException e) {
      throw new SerializationException(e);
    }
    return ChunkRegion.create(Region.of(min, max), checksum);
  }

  @Override
  public void serialize(Type type, ChunkRegion.@Nullable Validated chunk, ConfigurationNode target) throws SerializationException {
    if (chunk == null) {
      target.raw(null);
      return;
    }
    target.node(MIN).set(Vector3i.class, chunk.region().min());
    target.node(MAX).set(Vector3i.class, chunk.region().max());
    target.node(CHECKSUM).set(Long.toHexString(chunk.checksum()).toUpperCase(Locale.ROOT));
  }
}
