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

package me.moros.gaia.common.service;

import java.util.Optional;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.moros.gaia.api.arena.region.Region;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.api.service.SelectionService;
import me.moros.math.Vector3i;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class WorldEditSelectionService implements SelectionService {
  @Override
  public void resetSelection(GaiaUser user) {
  }

  @Override
  public Optional<Region> selection(GaiaUser user) {
    var player = adapt(user);
    if (player != null) {
      var session = WorldEdit.getInstance().getSessionManager().getIfPresent(player);
      if (session != null) {
        com.sk89q.worldedit.regions.Region selectedRegion;
        try {
          selectedRegion = session.getSelection(player.getWorld());
          if (selectedRegion instanceof CuboidRegion cr) {
            var min = Vector3i.of(cr.getMinimumPoint().getX(), cr.getMinimumPoint().getY(), cr.getMinimumPoint().getZ());
            var max = Vector3i.of(cr.getMaximumPoint().getX(), cr.getMaximumPoint().getY(), cr.getMaximumPoint().getZ());
            return Optional.of(Region.of(min, max));
          }
        } catch (IncompleteRegionException ignore) {
        }
      }
    }
    return Optional.empty();
  }

  protected abstract @Nullable Player adapt(GaiaUser user);
}
