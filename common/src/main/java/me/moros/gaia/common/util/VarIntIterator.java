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

package me.moros.gaia.common.util;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/**
 * Simple class to transform a {@code byte[]} into an iterator of the VarInts encoded in it.
 */
public class VarIntIterator implements PrimitiveIterator.OfInt {
  private final byte[] source;
  private int remaining;
  private int index;

  public VarIntIterator(byte[] source, int expected) {
    this.source = source;
    this.remaining = expected;
  }

  public int index() {
    return index;
  }

  @Override
  public boolean hasNext() {
    return remaining > 0;
  }

  private int readNextInt() {
    int value = 0;
    for (int bitsRead = 0; ; bitsRead += 7) {
      byte next = source[index];
      index++;
      value |= (next & 0x7F) << bitsRead;
      if ((next & 0x80) == 0) {
        break;
      }
    }
    remaining--;
    return value;
  }

  @Override
  public int nextInt() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return readNextInt();
  }
}
