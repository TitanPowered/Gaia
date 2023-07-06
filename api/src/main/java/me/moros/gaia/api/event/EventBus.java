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

package me.moros.gaia.api.event;

import java.util.function.Consumer;

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.region.ChunkRegion;
import net.kyori.adventure.key.Key;

/**
 * The event bus is responsible for posting gaia events.
 */
public interface EventBus {
  /**
   * Close this event bus preventing any further events from being posted.
   */
  void shutdown();

  /**
   * Registers the given subscriber to receive events.
   * @param event the event type
   * @param subscriber the subscriber
   * @param <T> the event type
   * @see #subscribe(Class, Consumer, int)
   */
  default <T extends GaiaEvent> void subscribe(Class<T> event, Consumer<? super T> subscriber) {
    subscribe(event, subscriber, 0);
  }

  /**
   * Registers the given subscriber to receive events.
   * @param event the event type
   * @param subscriber the subscriber
   * @param priority the subscriber's priority, default priority is 0
   * @param <T> the event type
   */
  <T extends GaiaEvent> void subscribe(Class<T> event, Consumer<? super T> subscriber, int priority);

  /**
   * Post an event.
   * @param event the event to post
   * @param <T> the type of event
   * @return true if the event was successfully posted, false otherwise
   */
  <T extends GaiaEvent> boolean post(T event);

  /**
   * Posts a new {@link ArenaAnalyzeEvent}.
   * @param arena the arena that was being analyzed
   * @param time the amount of time it took to analyze the arena in milliseconds
   */
  ArenaAnalyzeEvent postArenaAnalyzeEvent(Arena arena, long time);

  /**
   * Posts a new {@link ArenaRevertEvent}.
   * @param arena the arena that was being reverted
   * @param time the amount of time it took to revert the arena in milliseconds
   * @param completed whether reverting was successful and operation has completed
   */
  ArenaRevertEvent postArenaRevertEvent(Arena arena, long time, boolean completed);

  /**
   * Posts a new {@link ChunkAnalyzeEvent}.
   * @param chunkRegion the chunk that was analyzed
   * @param level level key
   * @param time the amount of time it took to analyze the chunk in milliseconds
   */
  ChunkAnalyzeEvent postChunkAnalyzeEvent(ChunkRegion chunkRegion, Key level, long time);

  /**
   * Posts a new {@link ChunkRevertEvent}.
   * @param chunkRegion the chunk that was reverted
   * @param level level key
   * @param time the amount of time it took to revert the chunk in milliseconds
   */
  ChunkRevertEvent postChunkRevertEvent(ChunkRegion chunkRegion, Key level, long time);
}
