/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *
 * 	  This file is part of Gaia.
 *
 *    Gaia is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Gaia is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Gaia.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.gaia;

import java.util.Iterator;
import java.util.UUID;

import me.moros.gaia.api.Arena;
import me.moros.gaia.api.GaiaChunk;
import me.moros.gaia.api.GaiaData;
import me.moros.gaia.api.GaiaRegion;
import me.moros.gaia.api.GaiaVector;
import me.moros.gaia.configuration.ConfigManager;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.platform.GaiaWorld;
import me.moros.gaia.platform.WorldWrapper;
import me.moros.gaia.util.functional.GaiaRunnableInfo;
import me.moros.gaia.util.metadata.ArenaMetadata;
import me.moros.gaia.util.metadata.ChunkMetadata;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PaperGaiaChunk extends GaiaChunk {
  protected PaperGaiaChunk(@NonNull UUID id, Arena parent, @NonNull GaiaRegion region) {
    super(id, parent, region);
  }

  @Override
  public void analyze(@NonNull GaiaRunnableInfo info, @NonNull GaiaData data) {
    ((WorldWrapper) info.world).get().getChunkAtAsync(getX(), getZ()).thenRun(() -> {
      GaiaVector relative, real;
      int counter = 0;
      while (++counter <= info.maxTransactions && info.it.hasNext()) {
        relative = info.it.next();
        real = getRegion().getMinimumPoint().add(relative);
        data.setDataAt(relative, info.world.getBlockAt(real).getBlockData());
      }
      if (info.it.hasNext()) {
        Bukkit.getScheduler().runTaskLater(Gaia.getPlugin(), () -> analyze(info, data), 1);
      } else {
        Bukkit.getScheduler().runTaskAsynchronously(Gaia.getPlugin(), () -> {
          String hash = GaiaIO.getInstance().saveData(this, data);
          if (!hash.isEmpty())
            ((ArenaMetadata) getParent().getMetadata()).chunks.add((ChunkMetadata) getMetadata());
        });
      }
    });
  }

  @Override
  public void revert(@NonNull GaiaRunnableInfo info, @NonNull GaiaData data) {
    if (!isReverting()) return;
    ((WorldWrapper) info.world).get().getChunkAtAsync(getX(), getZ()).thenRun(() -> {
      GaiaVector relative, real;
      int counter = 0;
      while (++counter <= info.maxTransactions && info.it.hasNext()) {
        relative = info.it.next();
        real = getRegion().getMinimumPoint().add(relative);
        info.world.getBlockAt(real).setBlockData(data.getDataAt(relative));
      }
      if (info.it.hasNext()) {
        Bukkit.getScheduler().runTaskLater(Gaia.getPlugin(), () -> revert(info, data), 1);
      } else {
        cancelReverting();
      }
    });
  }

  public static void revertChunk(@NonNull GaiaChunk chunk, @NonNull GaiaWorld world) {
    if (chunk.isReverting()) return;
    chunk.startReverting();
    Bukkit.getScheduler().runTaskAsynchronously(Gaia.getPlugin(), () -> {
      final Iterator<GaiaVector> it = chunk.iterator();
      final GaiaData gd = GaiaIO.getInstance().loadData(chunk);
      if (gd != null)
        chunk.revert(new GaiaRunnableInfo(it, world, ConfigManager.INSTANCE.getConcurrentTransactions()), gd);
    });
  }

  public static void analyzeChunk(@NonNull GaiaChunk chunk, @NonNull GaiaWorld world) {
    if (chunk.isAnalyzed()) return;
    final Iterator<GaiaVector> it = chunk.iterator();
    final GaiaData gd = new GaiaData(chunk.getRegion().getVector());
    chunk.analyze(new GaiaRunnableInfo(it, world, ConfigManager.INSTANCE.getConcurrentTransactions()), gd);
  }
}
