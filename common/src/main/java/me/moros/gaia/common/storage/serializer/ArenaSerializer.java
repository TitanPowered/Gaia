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

package me.moros.gaia.common.storage.serializer;

import java.lang.reflect.Type;
import java.util.List;

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.math.Vector3i;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

final class ArenaSerializer extends AbstractSerializer<Arena> {
  static final ArenaSerializer INSTANCE = new ArenaSerializer();

  private static final int VERSION_VALUE = 2;

  private static final String VERSION = "version";
  private static final String NAME = "name";
  private static final String LEVEL = "level";
  private static final String MIN = "min";
  private static final String MAX = "max";
  private static final String AMOUNT = "amount";
  private static final String CHUNKS = "chunks";
  private static final String POINTS = "points";

  private ArenaSerializer() {
  }

  @Override
  public Arena deserialize(Type type, ConfigurationNode source) throws SerializationException {
    int version = nonVirtualNode(source, VERSION).getInt();
    if (version != VERSION_VALUE) {
      throw new SerializationException("Unknown data version, expected %d but found %d".formatted(VERSION_VALUE, version));
    }
    String name = nonVirtualNode(source, NAME).getString();
    //noinspection PatternValidation
    Key level = Key.key(nonVirtualNode(source, LEVEL).getString(""));
    Vector3i min = nonVirtualNode(source, MIN).get(Vector3i.class);
    Vector3i max = nonVirtualNode(source, MAX).get(Vector3i.class);
    if (name == null || min == null || max == null) {
      throw new SerializationException("A name, level, min and max are required to deserialize an Arena");
    }
    var region = Region.of(min, max);
    int amount = nonVirtualNode(source, AMOUNT).getInt();
    List<ChunkRegion.Validated> chunks = nonVirtualNode(source, CHUNKS).getList(ChunkRegion.Validated.class, List::of);
    if (amount != chunks.size()) {
      throw new SerializationException("Expected %d chunk regions but found %d".formatted(amount, chunks.size()));
    }
    List<Point> points = nonVirtualNode(source, POINTS).getList(Point.class, List::of);
    return Arena.builder().name(name).level(level).region(region).chunks(chunks).points(points).build();
  }

  @Override
  public void serialize(Type type, @Nullable Arena arena, ConfigurationNode target) throws SerializationException {
    if (arena == null) {
      target.raw(null);
      return;
    }
    target.node(VERSION).set(VERSION_VALUE);
    target.node(NAME).set(arena.name());
    target.node(LEVEL).set(arena.level().asString());
    target.node(MIN).set(Vector3i.class, arena.region().min());
    target.node(MAX).set(Vector3i.class, arena.region().max());
    target.node(AMOUNT).set(arena.chunks().size());
    target.node(CHUNKS).setList(ChunkRegion.Validated.class, arena.chunks());
    target.node(POINTS).setList(Point.class, arena.points());
  }
}
