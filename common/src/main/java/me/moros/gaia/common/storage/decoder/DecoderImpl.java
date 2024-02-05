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

package me.moros.gaia.common.storage.decoder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.common.platform.GaiaSnapshot;
import me.moros.gaia.common.util.BlockStateCodec;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.slf4j.Logger;

final class DecoderImpl implements Decoder {
  private final int dataVersion;
  private final Logger logger;
  private final Map<String, BlockState> cache;

  DecoderImpl(Logger logger) {
    this.dataVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    this.logger = logger;
    this.cache = new ConcurrentHashMap<>();
    rebuildCache();
  }

  private void rebuildCache() {
    cache.clear();
    Block.BLOCK_STATE_REGISTRY.forEach(state -> cache.put(BlockStateCodec.INSTANCE.toString(state), state));
  }

  @Override
  public int dataVersion() {
    return dataVersion;
  }

  @Override
  public Snapshot decodeBlocks(ChunkRegion chunk, LinCompoundTag paletteObject, byte[] blocks, int srcVersion) throws IOException {
    var palette = decodePalette(paletteObject, srcVersion);
    return new GaiaSnapshot(chunk, palette, blocks);
  }

  private Int2ObjectMap<BlockState> decodePalette(LinCompoundTag paletteObject, int srcVersion) throws IOException {
    var entrySet = paletteObject.value().entrySet();
    Int2ObjectMap<BlockState> palette = new Int2ObjectArrayMap<>(entrySet.size());
    for (var palettePart : entrySet) {
      if (palettePart.getValue() instanceof LinIntTag idTag) {
        palette.put(idTag.valueAsInt(), readAndFixBlockState(palettePart.getKey(), srcVersion));
      } else {
        throw new IOException("Invalid palette entry: " + palettePart);
      }
    }
    return palette;
  }

  private BlockState readAndFixBlockState(String value, int srcVersion) {
    BlockState result = cache.computeIfAbsent(value, v -> parseStateForCache(v, srcVersion));
    if (result == null) {
      logger.warn("Invalid BlockState: " + value + ". Block will be replaced with air.");
      return Blocks.AIR.defaultBlockState();
    } else {
      return result;
    }
  }

  private @Nullable BlockState parseStateForCache(String data, int srcVersion) {
    CompoundTag nbt = BlockStateCodec.INSTANCE.stateToNBT(data);
    CompoundTag result = (CompoundTag) DataFixers.getDataFixer()
      .update(References.BLOCK_STATE, new Dynamic<>(NbtOps.INSTANCE, nbt), srcVersion, dataVersion)
      .getValue();
    return BlockStateCodec.INSTANCE.nbtToState(result);
  }
}
