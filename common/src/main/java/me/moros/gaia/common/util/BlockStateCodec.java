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

package me.moros.gaia.common.util;

import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum BlockStateCodec {
  INSTANCE;

  public String toString(BlockState state) {
    StringBuilder sb = new StringBuilder();
    sb.append(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    if (!state.getValues().isEmpty()) {
      sb.append('[');
      sb.append(state.getValues().entrySet().stream().map(this::propertyMapper).collect(Collectors.joining(",")));
      sb.append(']');
    }
    return sb.toString();
  }

  public CompoundTag stateToNBT(String blockState) {
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

  public @Nullable BlockState nbtToState(CompoundTag nbt) {
    if (!nbt.contains("Name", 8)) {
      return null;
    } else {
      ResourceLocation rsl = ResourceLocation.tryParse(nbt.getString("Name"));
      Block block = rsl == null ? null : BuiltInRegistries.BLOCK.asLookup()
        .get(ResourceKey.create(Registries.BLOCK, rsl)).map(Reference::value).orElse(null);
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
  private <T extends Comparable<T>> BlockState setValue(BlockState state, Property<T> property, Object value) {
    return state.setValue(property, (T) value);
  }

  private String propertyMapper(Map.Entry<Property<?>, Comparable<?>> entry) {
    Property<?> property = entry.getKey();
    return property.getName() + "=" + getName(property, entry.getValue());
  }

  @SuppressWarnings("unchecked")
  private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> value) {
    return property.getName((T) value);
  }
}
