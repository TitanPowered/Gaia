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

package me.moros.gaia.common.event;

import java.util.function.Consumer;

import com.seiama.event.Cancellable;
import com.seiama.event.EventConfig;
import com.seiama.event.bus.SimpleEventBus;
import com.seiama.event.registry.EventRegistry;
import com.seiama.event.registry.SimpleEventRegistry;
import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.event.ArenaAnalyzeEvent;
import me.moros.gaia.api.event.ArenaRevertEvent;
import me.moros.gaia.api.event.ChunkAnalyzeEvent;
import me.moros.gaia.api.event.ChunkRevertEvent;
import me.moros.gaia.api.event.EventBus;
import me.moros.gaia.api.event.GaiaEvent;
import net.kyori.adventure.key.Key;
import org.slf4j.Logger;

public class EventBusImpl implements EventBus {
  private final EventRegistry<GaiaEvent> eventRegistry;
  private final com.seiama.event.bus.EventBus<GaiaEvent> eventBus;
  private boolean closed = false;

  public EventBusImpl(Logger logger) {
    this.eventRegistry = new SimpleEventRegistry<>(GaiaEvent.class);
    this.eventBus = new SimpleEventBus<>(eventRegistry, new EventExceptionHandlerImpl(logger));
  }

  @Override
  public void shutdown() {
    this.eventRegistry.unsubscribeIf(x -> true);
    this.closed = true;
  }

  @Override
  public <T extends GaiaEvent> void subscribe(Class<T> event, Consumer<? super T> subscriber, int priority) {
    if (!closed) {
      var eventConfig = EventConfig.of(priority, false, false);
      eventRegistry.subscribe(event, eventConfig, new EventSubscriberImpl<>(subscriber));
    }
  }

  @Override
  public <T extends GaiaEvent> boolean post(T event) {
    if (closed) {
      throw new IllegalStateException("Eventbus has been terminated, cannot post new events!");
    }
    eventBus.post(event);
    return !(event instanceof Cancellable c) || !c.cancelled();
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

