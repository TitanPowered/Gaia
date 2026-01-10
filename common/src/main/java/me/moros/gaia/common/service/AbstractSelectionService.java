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

package me.moros.gaia.common.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.moros.gaia.api.arena.region.Region;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.api.service.SelectionService;
import me.moros.math.Vector3i;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractSelectionService implements SelectionService {
  private final Map<UUID, Selection> cache;

  protected AbstractSelectionService() {
    this.cache = new ConcurrentHashMap<>();
  }

  @Override
  public void resetSelection(GaiaUser user) {
    user.get(Identity.UUID).ifPresent(this::invalidate);
  }

  protected void invalidate(UUID uuid) {
    cache.remove(uuid);
  }

  protected void registerClick(UUID uuid, Key level, Vector3i point) {
    updateAndGet(uuid, level).registerPos(point, true);
  }

  protected void registerInteraction(UUID uuid, Key level, Vector3i point) {
    updateAndGet(uuid, level).registerPos(point, false);
  }

  private Selection updateAndGet(UUID uuid, Key level) {
    return cache.compute(uuid, (key, oldSel) -> {
      if (oldSel == null || !oldSel.level.equals(level)) {
        return new Selection(level);
      } else {
        return oldSel;
      }
    });
  }

  @Override
  public Optional<Region> selection(GaiaUser user) {
    var levelKey = user.level().orElse(null);
    if (user.isPlayer() && levelKey != null) {
      return user.get(Identity.UUID).map(cache::get).filter(s -> s.level().equals(levelKey)).flatMap(Selection::asRegion);
    }
    return Optional.empty();
  }

  private static final class Selection {
    private final Key level;
    private Vector3i pos1;
    private Vector3i pos2;

    private Selection(Key level) {
      this.level = level;
    }

    private Key level() {
      return level;
    }

    private void registerPos(@Nullable Vector3i pos, boolean primary) {
      if (primary) {
        this.pos1 = pos;
      } else {
        this.pos2 = pos;
      }
    }

    private Optional<Region> asRegion() {
      if (pos1 == null || pos2 == null) {
        return Optional.empty();
      }
      return Optional.of(Region.of(pos1, pos2));
    }
  }
}
