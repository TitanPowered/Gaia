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

package me.moros.gaia.common.config;

import me.moros.gaia.api.util.LightFixer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public record Config(
  @Comment("The maximum amount of time in milliseconds for snapshot analysis")
  long timeout,
  @Comment("The cooldown in milliseconds before an arena can be reverted again")
  long cooldown,
  @Comment("The maximum amount of chunks that will be restored every tick")
  int concurrentChunks,
  @Comment("The maximum amount of chunk sections that will be restored every tick per chunk")
  int sectionsPerTick,
  @Comment("Light fixer can optionally queue light recalculations for reverted chunks/arenas in a 2nd pass. Available options: DISABLED, POST-CHUNK, POST-ARENA")
  LightFixer lightFixer) {
  public Config(long timeout, long cooldown, int concurrentChunks, int sectionsPerTick, @Nullable LightFixer lightFixer) {
    this.timeout = timeout > 0 ? timeout : 30_000;
    this.cooldown = cooldown > 0 ? cooldown : 5000;
    this.concurrentChunks = concurrentChunks > 0 ? concurrentChunks : 16;
    this.sectionsPerTick = sectionsPerTick > 0 ? sectionsPerTick : 24;
    this.lightFixer = lightFixer == null ? LightFixer.POST_ARENA : lightFixer;
  }

  Config() {
    this(30_000, 5000, 16, 24, LightFixer.POST_ARENA);
  }
}
