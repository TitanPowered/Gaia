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

package me.moros.gaia.common.util;

import java.lang.reflect.Method;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.GaiaProvider;

public final class GaiaProviderUtil {
  private GaiaProviderUtil() {
  }

  private static final Method REGISTER;
  private static final Method UNREGISTER;

  static {
    try {
      REGISTER = GaiaProvider.class.getDeclaredMethod("register", Gaia.class);
      REGISTER.setAccessible(true);

      UNREGISTER = GaiaProvider.class.getDeclaredMethod("unregister");
      UNREGISTER.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static void registerProvider(Gaia gaia) {
    try {
      REGISTER.invoke(null, gaia);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void unregisterProvider() {
    try {
      UNREGISTER.invoke(null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
