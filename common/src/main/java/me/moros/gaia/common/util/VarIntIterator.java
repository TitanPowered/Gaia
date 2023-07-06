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

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/**
 * Simple class to transform a {@code byte[]} into an iterator of the VarInts encoded in it.
 */
public class VarIntIterator implements PrimitiveIterator.OfInt {
  private final byte[] source;
  private int index;
  private boolean hasNextInt;
  private int nextInt;

  public VarIntIterator(byte[] source) {
    this.source = source;
  }

  public int index() {
    return index;
  }

  @Override
  public boolean hasNext() {
    if (hasNextInt) {
      return true;
    }
    if (index >= source.length) {
      return false;
    }
    nextInt = readNextInt();
    return hasNextInt = true;
  }

  private int readNextInt() {
    int value = 0;
    for (int bitsRead = 0; ; bitsRead += 7) {
      if (index >= source.length) {
        throw new IllegalStateException("Ran out of bytes while reading VarInt (probably corrupted data)");
      }
      byte next = source[index];
      index++;
      value |= (next & 0x7F) << bitsRead;
      if (bitsRead > 7 * 5) {
        throw new IllegalStateException("VarInt too big (probably corrupted data)");
      }
      if ((next & 0x80) == 0) {
        break;
      }
    }
    return value;
  }

  @Override
  public int nextInt() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    hasNextInt = false;
    return nextInt;
  }
}
