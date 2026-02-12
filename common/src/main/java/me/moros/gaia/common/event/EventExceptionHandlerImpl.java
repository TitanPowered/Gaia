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

package me.moros.gaia.common.event;

import com.sasorio.event.EventSubscription;
import com.sasorio.event.bus.EventBus;
import com.sasorio.event.bus.EventBus.EventExceptionHandler;
import org.slf4j.Logger;

record EventExceptionHandlerImpl(Logger logger) implements EventExceptionHandler {
  @Override
  public <E> void eventExceptionCaught(EventBus<? super E> bus, EventSubscription<? super E> subscription, E event, Throwable throwable) {
    logger.warn("Exception posting event %s to subscriber %s".formatted(event, subscription.subscriber()), throwable);
  }
}
