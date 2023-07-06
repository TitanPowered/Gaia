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

package me.moros.gaia.paper.service;

import java.util.UUID;

import me.moros.gaia.common.locale.Message;
import me.moros.gaia.common.service.AbstractSelectionService;
import me.moros.math.Vector3i;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public final class GaiaSelectionService extends AbstractSelectionService implements Listener {
  public GaiaSelectionService(JavaPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.useItemInHand() == Result.DENY || event.getHand() == EquipmentSlot.OFF_HAND) {
      return;
    }
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();
    Key level = player.getWorld().key();
    Action action = event.getAction();
    final Block clickedBlock = event.getClickedBlock();
    if (clickedBlock == null || player.getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE) {
      return;
    }
    var pos = Vector3i.of(clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());
    if (action == Action.LEFT_CLICK_BLOCK) {
      registerClick(uuid, level, pos);
      Message.SELECTION_FIRST.send(player, pos.toString());
      event.setCancelled(true);
    } else if (action == Action.RIGHT_CLICK_BLOCK) {
      registerInteraction(uuid, level, pos);
      Message.SELECTION_SECOND.send(player, pos.toString());
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    invalidate(event.getPlayer().getUniqueId());
  }
}
