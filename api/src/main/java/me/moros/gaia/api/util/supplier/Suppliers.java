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

package me.moros.gaia.api.util.supplier;

import java.util.function.Supplier;

public final class Suppliers {
  private Suppliers() {
  }

  public static <T> Supplier<T> lazy(Supplier<T> supplier) {
    return new LazyCachingSupplier<>(supplier);
  }

  public static <T> Supplier<T> cached(T value) {
    return new CachingSupplier<>(value);
  }
}
