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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class FutureUtil {
  private FutureUtil() {
  }

  public static <T> CompletableFuture<Void> createFailFast(Collection<? extends CompletableFuture<? extends T>> futures) {
    var allOf = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    for (var f : futures) {
      cancel(f, allOf);
    }
    return allOf;
  }

  public static <T> CompletableFuture<List<T>> createFailFastBatch(Collection<? extends CompletableFuture<? extends T>> futures) {
    return createFailFast(futures)
      .thenApply(ignored -> {
        final List<T> result = new ArrayList<>(futures.size());
        futures.forEach(f -> result.add(f.join()));
        return result;
      });
  }

  private static void cancel(CompletableFuture<?> future, CompletableFuture<?> allOf) {
    future.exceptionally(e -> {
      if (!allOf.isDone()) {
        allOf.completeExceptionally(e);
      }
      return null;
    });
  }
}
