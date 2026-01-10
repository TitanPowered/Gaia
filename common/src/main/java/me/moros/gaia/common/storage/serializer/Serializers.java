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

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.math.Vector3i;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

public final class Serializers {
  private Serializers() {
  }

  public static final TypeSerializerCollection ALL = TypeSerializerCollection.builder()
    .register(Vector3i.class, Vector3iSerializer.INSTANCE)
    .register(Point.class, PointSerializer.INSTANCE)
    .register(ChunkRegion.Validated.class, ChunkRegionSerializer.INSTANCE)
    .register(Arena.class, ArenaSerializer.INSTANCE)
    .build();
}
