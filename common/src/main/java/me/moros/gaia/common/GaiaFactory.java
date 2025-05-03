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

package me.moros.gaia.common;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import me.moros.gaia.api.util.supplier.Suppliers;

public final class GaiaFactory {
  private final Map<Class<?>, Supplier<?>> factoryMappings;

  public GaiaFactory() {
    this.factoryMappings = new ConcurrentHashMap<>();
  }

  public <T> GaiaFactory bind(Class<T> type, Supplier<? extends T> supplier) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(supplier);
    factoryMappings.put(type, Suppliers.lazy(supplier));
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T build(Class<T> type) {
    var supplier = factoryMappings.get(type);
    if (supplier == null) {
      throw new IllegalArgumentException("No mapping found to construct " + type.getName());
    }
    return (T) supplier.get();
  }
}
