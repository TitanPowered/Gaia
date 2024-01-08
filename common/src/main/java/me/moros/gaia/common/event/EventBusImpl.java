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

package me.moros.gaia.common.event;

import java.util.function.Consumer;

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.event.ArenaAnalyzeEvent;
import me.moros.gaia.api.event.ArenaRevertEvent;
import me.moros.gaia.api.event.ChunkAnalyzeEvent;
import me.moros.gaia.api.event.ChunkRevertEvent;
import me.moros.gaia.api.event.EventBus;
import me.moros.gaia.api.event.GaiaEvent;
import net.kyori.adventure.key.Key;

public class EventBusImpl implements EventBus {
  private final net.kyori.event.EventBus<GaiaEvent> eventBus;
  private boolean closed = false;

  public EventBusImpl() {
    this.eventBus = net.kyori.event.EventBus.create(GaiaEvent.class);
  }

  @Override
  public void shutdown() {
    this.eventBus.unsubscribeIf(x -> true);
    this.closed = true;
  }

  @Override
  public <T extends GaiaEvent> void subscribe(Class<T> event, Consumer<? super T> subscriber, int priority) {
    if (!closed) {
      eventBus.subscribe(event, new EventSubscriberImpl<>(subscriber, priority));
    }
  }

  @Override
  public <T extends GaiaEvent> boolean post(T event) {
    if (closed) {
      throw new IllegalStateException("Eventbus has been terminated, cannot post new events!");
    }
    return eventBus.post(event).wasSuccessful();
  }

  private <T extends GaiaEvent> T postAndReturn(T event) {
    post(event);
    return event;
  }

  @Override
  public ArenaAnalyzeEvent postArenaAnalyzeEvent(Arena arena, long time) {
    return postAndReturn(new ArenaAnalyzeEventImpl(arena, time));
  }

  @Override
  public ArenaRevertEvent postArenaRevertEvent(Arena arena, long time, boolean completed) {
    return postAndReturn(new ArenaRevertEventImpl(arena, time, completed));
  }

  @Override
  public ChunkAnalyzeEvent postChunkAnalyzeEvent(ChunkRegion chunk, Key level, long time) {
    return postAndReturn(new ChunkAnalyzeEventImpl(chunk, level, time));
  }

  @Override
  public ChunkRevertEvent postChunkRevertEvent(ChunkRegion chunk, Key level, long time) {
    return postAndReturn(new ChunkRevertEventImpl(chunk, level, time));
  }
}

