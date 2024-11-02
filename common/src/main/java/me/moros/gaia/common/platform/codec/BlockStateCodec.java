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

package me.moros.gaia.common.platform.codec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.mojang.serialization.Dynamic;
import me.moros.gaia.api.util.supplier.Suppliers;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;

final class BlockStateCodec implements SimpleCodec<BlockState> {
  static final SimpleCodec<BlockState> INSTANCE = new BlockStateCodec();

  private final int dataVersion;
  private final Logger logger;
  private final Supplier<Map<String, BlockState>> cache;

  private BlockStateCodec() {
    this.dataVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    this.logger = LoggerFactory.getLogger("Gaia");
    this.cache = Suppliers.lazy(BlockStateCodec::buildCache);
  }

  @Override
  public int dataVersion() {
    return dataVersion;
  }

  @Override
  public String toString(BlockState value) {
    return convertToString(value);
  }

  @Override
  public @Nullable BlockState fromString(String raw, int srcVersion) {
    BlockState result = cache.get().computeIfAbsent(raw, v -> calculateBlockState(v, srcVersion));
    if (result == null) {
      logger.warn("Invalid BlockState: " + raw + ". Block will be replaced with air.");
      return Blocks.AIR.defaultBlockState();
    } else {
      return result;
    }
  }

  private @Nullable BlockState calculateBlockState(String data, int srcVersion) {
    CompoundTag nbt = stateToNBT(data);
    CompoundTag result = (CompoundTag) DataFixers.getDataFixer()
      .update(References.BLOCK_STATE, new Dynamic<>(NbtOps.INSTANCE, nbt), srcVersion, dataVersion)
      .getValue();
    return nbtToState(result);
  }

  private CompoundTag stateToNBT(String blockState) {
    int propIdx = blockState.indexOf('[');
    CompoundTag tag = new CompoundTag();
    if (propIdx < 0) {
      tag.putString("Name", blockState);
    } else {
      tag.putString("Name", blockState.substring(0, propIdx));
      CompoundTag propTag = new CompoundTag();
      String props = blockState.substring(propIdx + 1, blockState.length() - 1);
      String[] propArr = props.split(",");
      for (String pair : propArr) {
        final String[] split = pair.split("=");
        propTag.putString(split[0], split[1]);
      }
      tag.put("Properties", propTag);
    }
    return tag;
  }

  private @Nullable BlockState nbtToState(CompoundTag nbt) {
    if (!nbt.contains("Name", 8)) {
      return null;
    } else {
      ResourceLocation rsl = ResourceLocation.tryParse(nbt.getString("Name"));
      Block block = rsl == null ? null : BuiltInRegistries.BLOCK.getValue(ResourceKey.create(Registries.BLOCK, rsl));
      if (block == null) {
        return null;
      }
      BlockState blockState = block.defaultBlockState();
      if (nbt.contains("Properties", 10)) {
        CompoundTag compoundTag = nbt.getCompound("Properties");
        StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
        for (String string : compoundTag.getAllKeys()) {
          Property<?> property = stateDefinition.getProperty(string);
          if (property != null) {
            var propertyValue = property.getValue(compoundTag.getString(string)).orElse(null);
            if (propertyValue != null) {
              blockState = setValue(blockState, property, propertyValue);
            }
          }
        }
      }
      return blockState;
    }
  }

  @SuppressWarnings("unchecked")
  private static <T extends Comparable<T>> BlockState setValue(BlockState state, Property<T> property, Object value) {
    return state.setValue(property, (T) value);
  }

  private static String propertyMapper(Map.Entry<Property<?>, Comparable<?>> entry) {
    Property<?> property = entry.getKey();
    return property.getName() + "=" + getName(property, entry.getValue());
  }

  @SuppressWarnings("unchecked")
  private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> value) {
    return property.getName((T) value);
  }

  private static String convertToString(BlockState state) {
    StringBuilder sb = new StringBuilder();
    sb.append(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    if (!state.getValues().isEmpty()) {
      sb.append('[');
      sb.append(state.getValues().entrySet().stream().map(BlockStateCodec::propertyMapper).collect(joining(",")));
      sb.append(']');
    }
    return sb.toString();
  }

  private static Map<String, BlockState> buildCache() {
    Map<String, BlockState> map = new ConcurrentHashMap<>(Block.BLOCK_STATE_REGISTRY.size());
    Block.BLOCK_STATE_REGISTRY.forEach(state -> map.put(convertToString(state), state));
    return map;
  }
}
