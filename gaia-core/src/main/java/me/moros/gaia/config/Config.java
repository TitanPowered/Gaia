/*
 * Copyright 2020-2022 Moros
 *
 * This file is part of Gaia.
 *
 * Gaia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gaia is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Gaia. If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.gaia.config;

import org.spongepowered.configurate.CommentedConfigurationNode;

public class Config {
  private final long timeout;
  private final long cooldown;
  private final int concurrentChunks;
  private final int concurrentTransactions;
  private final boolean debug;

  Config(CommentedConfigurationNode rootNode) {
    timeout = rootNode.node("Analysis", "Timeout").getLong(30_000);
    cooldown = rootNode.node("Cooldown").getLong(5000);
    concurrentChunks = rootNode.node("ConcurrentChunks").getInt(4);
    concurrentTransactions = rootNode.node("ConcurrentTransactions").getInt(32768);
    debug = rootNode.node("Debug").getBoolean(false);
  }

  public long timeout() {
    return timeout;
  }

  public long cooldown() {
    return cooldown;
  }

  public int concurrentChunks() {
    return concurrentChunks;
  }

  public int concurrentTransactions() {
    return concurrentTransactions;
  }

  public boolean debug() {
    return debug;
  }
}
