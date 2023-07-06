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

package me.moros.gaia.api.util.supplier;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

final class LazyCachingSupplier<T> implements Supplier<T> {
  private Object value;

  LazyCachingSupplier(Supplier<T> supplier) {
    this.value = new RefInfo<>(new ReentrantLock(), supplier);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T get() {
    Object v = value;
    if (!(v instanceof RefInfo)) {
      return (T) v;
    }
    RefInfo<T> refInfo = (RefInfo<T>) v;
    refInfo.lock.lock();
    try {
      v = value;
      if (!(v instanceof RefInfo)) {
        return (T) v;
      }
      value = v = refInfo.supplier.get();
      return (T) v;
    } finally {
      refInfo.lock.unlock();
    }
  }

  private record RefInfo<T>(Lock lock, Supplier<T> supplier) {
  }
}

