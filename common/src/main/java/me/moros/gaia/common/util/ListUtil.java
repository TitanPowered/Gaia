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

package me.moros.gaia.common.util;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

import me.moros.math.FastMath;

public final class ListUtil {
  private ListUtil() {
  }

  public static <T> List<List<T>> partition(List<T> list, int size) {
    Objects.requireNonNull(list);
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be greater than 0");
    }
    return new Partition<>(list, size);
  }

  private static final class Partition<T> extends AbstractList<List<T>> {
    private final List<T> list;
    private final int size;

    private Partition(final List<T> list, final int size) {
      this.list = list;
      this.size = size;
    }

    @Override
    public List<T> get(final int index) {
      final int listSize = size();
      if (index < 0 || index >= listSize) {
        throw new IndexOutOfBoundsException(index);
      }
      final int start = index * size;
      final int end = Math.min(start + size, list.size());
      return list.subList(start, end);
    }

    @Override
    public boolean isEmpty() {
      return list.isEmpty();
    }

    @Override
    public int size() {
      return FastMath.ceil(list.size() / (double) size);
    }
  }
}
