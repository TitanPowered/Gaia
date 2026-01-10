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

package me.moros.gaia.paper.service;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.common.service.WorldEditSelectionService;
import net.kyori.adventure.identity.Identity;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BukkitWorldEditSelectionService extends WorldEditSelectionService {
  @Override
  protected @Nullable Player adapt(GaiaUser user) {
    return user.get(Identity.UUID).map(Bukkit::getPlayer).map(BukkitAdapter::adapt).orElse(null);
  }
}
