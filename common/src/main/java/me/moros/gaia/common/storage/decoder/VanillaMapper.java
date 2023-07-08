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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.moros.gaia.common.util.BlockStateCodec;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
    try {
      StringReader reader = new StringReader(data);
      var arg = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), reader, false);
      if (reader.canRead()) {
        return arg.blockState();
      }
    } catch (CommandSyntaxException ignore) {
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
}
