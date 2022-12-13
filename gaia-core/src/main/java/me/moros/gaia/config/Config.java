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

package me.moros.gaia.config;

import me.moros.gaia.LightFixer.Mode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record Config(long timeout, long cooldown, int concurrentChunks, int concurrentTransactions, boolean debug,
                     Mode lightFixer) {
  public Config(long timeout, long cooldown, int concurrentChunks, int concurrentTransactions, boolean debug, @Nullable Mode lightFixer) {
    this.timeout = timeout > 0 ? timeout : 30_000;
    this.cooldown = cooldown > 0 ? cooldown : 5000;
    this.concurrentChunks = concurrentChunks > 0 ? concurrentChunks : 4;
    this.concurrentTransactions = concurrentTransactions > 0 ? concurrentTransactions : 32_768;
    this.debug = debug;
    this.lightFixer = lightFixer == null ? Mode.POST_ARENA : lightFixer;
  }

  Config() {
    this(30_000, 5000, 4, 32_768, false, Mode.POST_ARENA);
  }
}
