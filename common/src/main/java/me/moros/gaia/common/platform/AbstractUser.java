/*
 * Copyright 2020-2025 Moros
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

package me.moros.gaia.common.platform;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.platform.GaiaUser;

public abstract class AbstractUser<V> implements GaiaUser {
  private final Gaia parent;
  private final V handle;

  protected AbstractUser(Gaia parent, V handle) {
    this.parent = parent;
    this.handle = handle;
  }

  public V handle() {
    return handle;
  }

  @Override
  public Gaia parent() {
    return parent;
  }
}
