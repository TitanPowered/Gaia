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

package me.moros.gaia.fabric.service;

import java.util.UUID;

import me.moros.gaia.common.command.CommandPermissions;
import me.moros.gaia.common.locale.Message;
import me.moros.gaia.common.service.AbstractSelectionService;
import me.moros.math.Vector3i;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.permission.PermissionChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class GaiaSelectionService extends AbstractSelectionService {
  public GaiaSelectionService() {
    AttackBlockCallback.EVENT.register(this::onLeftClickBlock);
    UseBlockCallback.EVENT.register(this::onRightClickBlock);
    ServerPlayConnectionEvents.DISCONNECT.register(this::onPlayerDisconnect);
  }

  private boolean canInteract(Player player, Level world, InteractionHand hand) {
    if (hand == InteractionHand.OFF_HAND || world.isClientSide()) {
      return false;
    }
    if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WOODEN_AXE)) {
      return false;
    }
    if (player instanceof ServerPlayer sp && sp.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
      return sp.get(PermissionChecker.POINTER)
        .map(c -> c.test(CommandPermissions.CREATE.toString()))
        .orElse(false);
    }
    return false;
  }

  private InteractionResult onLeftClickBlock(Player player, Level world, InteractionHand hand, BlockPos blockPos, Direction direction) {
    if (canInteract(player, world, hand)) {
      UUID uuid = player.getUUID();
      Key level = world.dimension().identifier();
      var pos = Vector3i.of(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      registerClick(uuid, level, pos);
      Message.SELECTION_FIRST.send((ServerPlayer) player, pos.toString());
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  private InteractionResult onRightClickBlock(Player player, Level world, InteractionHand hand, BlockHitResult blockHitResult) {
    if (canInteract(player, world, hand)) {
      UUID uuid = player.getUUID();
      Key level = world.dimension().identifier();
      var blockPos = blockHitResult.getBlockPos();
      var pos = Vector3i.of(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      registerInteraction(uuid, level, pos);
      Message.SELECTION_SECOND.send((ServerPlayer) player, pos.toString());
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  private void onPlayerDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server) {
    invalidate(handler.getPlayer().getUUID());
  }
}
