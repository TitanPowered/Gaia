/*
 * Copyright 2020-2022 Moros
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

package me.moros.gaia.event;

import com.sk89q.worldedit.event.Cancellable;
import com.sk89q.worldedit.event.Event;
import me.moros.gaia.api.Arena;

public class ArenaAnalyzeEvent extends Event implements Cancellable {
  private final Arena arena;
  private final long analyzeTime;
  private boolean cancelled;

  public ArenaAnalyzeEvent(Arena arena, long analyzeTime) {
    this.arena = arena;
    this.analyzeTime = analyzeTime;
  }

  public Arena arena() {
    return arena;
  }

  public long analyzeTime() {
    return analyzeTime;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
  }
}
