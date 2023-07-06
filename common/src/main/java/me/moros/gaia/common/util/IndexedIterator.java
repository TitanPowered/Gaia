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

package me.moros.gaia.common.util;

import java.util.Iterator;
import java.util.function.IntFunction;

public final class IndexedIterator<T> implements Iterator<T> {
  private final IntFunction<T> mapper;
  private final VarIntIterator iterator;

  public IndexedIterator(VarIntIterator iterator, IntFunction<T> mapper) {
    this.iterator = iterator;
    this.mapper = mapper;
  }

  public int index() {
    return iterator.index();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public T next() {
    return mapper.apply(iterator.nextInt());
  }
}
