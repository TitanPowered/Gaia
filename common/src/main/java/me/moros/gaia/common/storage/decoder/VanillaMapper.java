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

package me.moros.gaia.common.storage.decoder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.moros.gaia.common.util.BlockStateCodec;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

record VanillaMapper(Logger logger, Map<String, BlockState> cache) implements Function<String, BlockState> {
  VanillaMapper(Logger logger) {
    this(logger, new ConcurrentHashMap<>());
  }

  VanillaMapper(Logger logger, Map<String, BlockState> cache) {
    this.logger = logger;
    this.cache = cache;
    rebuildCache();
  }

  private void rebuildCache() {
    cache.clear();
    Block.BLOCK_STATE_REGISTRY.forEach(state -> cache.put(BlockStateCodec.INSTANCE.toString(state), state));
  }

  private @Nullable BlockState parseStateForCache(String data) {
    StringReader reader = new StringReader(data);
    if (reader.canRead()) {
      Block block = readBlock(reader);
      if (block != null) {
        try {
          return readProperties(block, reader);
        } catch (CommandSyntaxException ignore) {
        }
      }
    }
    return null;
  }

  @Override
  public BlockState apply(String value) {
    // TODO use DFU
    BlockState result = cache.computeIfAbsent(value, this::parseStateForCache);
    if (result == null) {
      logger.warn("Invalid BlockState: " + value + ". Block will be replaced with air.");
      return Blocks.AIR.defaultBlockState();
    } else {
      return result;
    }
  }

  private @Nullable Block readBlock(StringReader reader) {
    int start = reader.getCursor();
    while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
      reader.skip();
    }
    String string = reader.getString().substring(start, reader.getCursor());
    ResourceLocation id = new ResourceLocation(string);
    return BuiltInRegistries.BLOCK.asLookup()
      .get(ResourceKey.create(Registries.BLOCK, id))
      .map(Reference::value)
      .orElse(null);
  }

  private @Nullable BlockState readProperties(Block block, StringReader reader) throws CommandSyntaxException {
    BlockState result = block.defaultBlockState();
    if (reader.canRead() && reader.peek() == '[') {
      reader.skip();
      reader.skipWhitespace();
      StateDefinition<Block, BlockState> definition = block.getStateDefinition();
      Map<Property<?>, Comparable<?>> properties = new HashMap<>();
      while (true) {
        if (reader.canRead() && reader.peek() != ']') {
          reader.skipWhitespace();
          String s = reader.readString();
          Property<?> property = definition.getProperty(s);
          if (property == null) {
            return null;
          }
          if (properties.containsKey(property)) {
            return null;
          }
          reader.skipWhitespace();
          if (!reader.canRead() || reader.peek() != '=') {
            return null;
          }
          reader.skip();
          reader.skipWhitespace();
          String raw = reader.readString();
          var propertyValue = property.getValue(raw).orElse(null);
          if (propertyValue == null) {
            return null;
          }
          result = setValue(result, property, propertyValue);
          properties.put(property, propertyValue);
          reader.skipWhitespace();
          if (!reader.canRead()) {
            continue;
          }
          if (reader.peek() == ',') {
            reader.skip();
            continue;
          }
          if (reader.peek() != ']') {
            return null;
          }
        }
        if (reader.canRead()) {
          reader.skip();
          return result;
        }
        return null;
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private <T extends Comparable<T>> BlockState setValue(BlockState state, Property<T> property, Object value) {
    return state.setValue(property, (T) value);
  }
}
