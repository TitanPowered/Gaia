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

package me.moros.gaia.api;

/**
 * Static singleton for conveniently accessing the {@link Gaia} instance.
 */
public final class GaiaProvider {
  private static Gaia INSTANCE = null;

  private GaiaProvider() {
  }

  /**
   * Gets an instance of the {@link Gaia} service.
   * @return the loaded service
   * @throws IllegalStateException if the service is not loaded
   */
  public static Gaia get() {
    if (INSTANCE == null) {
      throw new IllegalStateException("Gaia Service is not loaded.");
    }
    return INSTANCE;
  }

  static void register(Gaia gaia) {
    INSTANCE = gaia;
  }

  static void unregister() {
    INSTANCE = null;
  }
}
