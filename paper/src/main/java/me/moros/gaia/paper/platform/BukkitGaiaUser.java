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

package me.moros.gaia.paper.platform;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.common.platform.AbstractUser;
import me.moros.gaia.paper.platform.GaiaPlayer.GaiaPlayerImpl;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;

public class BukkitGaiaUser extends AbstractUser<CommandSourceStack> {
  private BukkitGaiaUser(Gaia parent, CommandSourceStack handle) {
    super(parent, handle);
  }

  @Override
  public Audience audience() {
    return handle().getSender();
  }

  public static final class BukkitGaiaPlayer extends BukkitGaiaUser implements GaiaPlayer {
    private final Player player;

    private BukkitGaiaPlayer(Gaia plugin, CommandSourceStack stack, Player player) {
      super(plugin, stack);
      this.player = player;
    }

    @Override
    public Player player() {
      return player;
    }
  }

  public static GaiaUser from(Gaia parent, CommandSourceStack stack) {
    if (stack.getSender() instanceof Player player) {
      return new BukkitGaiaPlayer(parent, stack, player);
    }
    return new BukkitGaiaUser(parent, stack);
  }

  public static GaiaUser from(Gaia parent, Player player) {
    return new GaiaPlayerImpl(parent, player);
  }
}
