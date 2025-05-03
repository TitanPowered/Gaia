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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class Debounced<R> {
  private final Supplier<R> supplier;
  private final long delay;
  private final TimeUnit timeUnit;
  private final CompletableFuture<R> future;
  private final AtomicReference<CompletableFuture<R>> taskRef;

  private Debounced(Supplier<R> supplier, long delay, TimeUnit timeUnit) {
    this.supplier = supplier;
    this.delay = delay;
    this.timeUnit = timeUnit;
    this.future = new CompletableFuture<>();
    this.taskRef = new AtomicReference<>();
  }

  public CompletableFuture<R> future() {
    return future;
  }

  public CompletableFuture<R> request() {
    taskRef.updateAndGet(this::createOrReschedule);
    return future;
  }

  private CompletableFuture<R> createOrReschedule(@Nullable CompletableFuture<R> taskFuture) {
    if (taskFuture != null) {
      taskFuture.cancel(false);
    }
    var delayedExecutor = CompletableFuture.delayedExecutor(delay, timeUnit);
    CompletableFuture<R> newFuture = CompletableFuture.supplyAsync(supplier, delayedExecutor);
    newFuture.thenAccept(future::complete);
    return newFuture;
  }

  public static <R> Debounced<R> create(Supplier<R> supplier, long delay, TimeUnit timeUnit) {
    return new Debounced<>(supplier, delay, timeUnit);
  }

  public static Debounced<?> create(Runnable runnable, long delay, TimeUnit timeUnit) {
    return create(() -> {
      runnable.run();
      return null;
    }, delay, timeUnit);
  }
}

