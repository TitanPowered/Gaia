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

package me.moros.gaia.common.command.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CancellationException;

import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.StringArgument.StringMode;
import cloud.commandframework.meta.CommandMeta;
import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.api.util.ComponentUtil;
import me.moros.gaia.api.util.TextUtil;
import me.moros.gaia.common.command.CommandPermissions;
import me.moros.gaia.common.command.Commander;
import me.moros.gaia.common.command.GaiaCommand;
import me.moros.gaia.common.command.argument.ArenaArgument;
import me.moros.gaia.common.command.argument.GaiaUserArgument;
import me.moros.gaia.common.config.ConfigManager;
import me.moros.gaia.common.locale.Message;
import me.moros.gaia.common.util.UserArenaFactory;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public record ArenaCommand(Commander commander) implements GaiaCommand {
  private static final int AMOUNT_PER_PAGE = 12;

  @Override
  public void register() {
    var builder = commander().rootBuilder();
    var arenaArg = ArenaArgument.builder("arena").asOptionalWithDefault("cur");
    var targetArg = GaiaUserArgument.builder("target").asOptionalWithDefault("me");
    commander().register(builder.literal("list", "ls")
      .meta(CommandMeta.DESCRIPTION, "List all Gaia arenas")
      .permission(CommandPermissions.LIST)
      .argument(IntegerArgument.optional("page", 1))
      .handler(c -> onList(c.getSender(), c.get("page")))
    );
    commander().register(builder.literal("info", "i")
      .meta(CommandMeta.DESCRIPTION, "View info about the specified arena")
      .permission(CommandPermissions.INFO)
      .argument(arenaArg.build())
      .handler(c -> onInfo(c.getSender(), c.get("arena")))
    );
    commander().register(builder.literal("create", "c")
      .meta(CommandMeta.DESCRIPTION, "Create a Gaia arena")
      .permission(CommandPermissions.CREATE)
      .argument(StringArgument.of("name", StringMode.SINGLE))
      .handler(c -> onCreate(c.getSender(), c.get("name")))
    );
    commander().register(builder.literal("remove", "rm")
      .meta(CommandMeta.DESCRIPTION, "Remove a Gaia arena")
      .permission(CommandPermissions.REMOVE)
      .argument(arenaArg.build())
      .handler(c -> onRemove(c.getSender(), c.get("arena")))
    );
    commander().register(builder.literal("revert", "reset")
      .meta(CommandMeta.DESCRIPTION, "Revert the specified arena")
      .permission(CommandPermissions.REVERT)
      .argument(arenaArg.build())
      .argument(targetArg.build())
      .handler(c -> onRevert(c.get("target"), c.get("arena")))
    );
    commander().register(builder.literal("cancel", "abort")
      .meta(CommandMeta.DESCRIPTION, "Cancel reverting the specified arena")
      .permission(CommandPermissions.CANCEL)
      .argument(arenaArg.build())
      .handler(c -> onCancel(c.getSender(), c.get("arena")))
    );
  }

  private void onList(GaiaUser user, Integer page) {
    int count = user.parent().arenaService().size();
    if (count == 0) {
      Message.LIST_NOT_FOUND.send(user);
      return;
    }
    int totalPages = (int) Math.ceil(count / (double) AMOUNT_PER_PAGE);
    if (page < 1 || page > totalPages) {
      Message.LIST_INVALID_PAGE.send(user);
      return;
    }
    int skip = (page - 1) * AMOUNT_PER_PAGE;
    TextComponent.Builder builder = Component.text().append(Component.text("Arenas - Page ", NamedTextColor.DARK_AQUA));
    if (page > 1) {
      builder.append(generatePaging(false, page - 1));
    }

    builder.append(Component.text(page, NamedTextColor.GREEN))
      .append(Component.text(" of ", NamedTextColor.DARK_AQUA))
      .append(Component.text(totalPages, NamedTextColor.GREEN));

    if (page < totalPages) {
      builder.append(generatePaging(true, page + 1));
    }
    user.sendMessage(builder.build());
    user.parent().arenaService().stream().sorted(Comparator.comparing(Arena::name)).
      skip(skip).limit(AMOUNT_PER_PAGE).map(ComponentUtil::arenaInfoAsHover).forEach(user::sendMessage);
    user.sendMessage(Component.text(TextUtil.generateLine(44), NamedTextColor.DARK_AQUA));
  }

  private void onInfo(GaiaUser user, Arena arena) {
    Message.INFO_HEADER.send(user);
    user.sendMessage(arena.info());
    List<Point> points = arena.points();
    if (points.isEmpty()) {
      return;
    }
    ListIterator<Point> it = points.listIterator();
    List<Component> components = new ArrayList<>();
    while (it.hasNext()) {
      components.add(ComponentUtil.generatePointInfo(arena, it.next(), it.nextIndex()));
    }
    JoinConfiguration sep = JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY));
    Message.LIST_POINTS.send(user, arena.displayName());
    user.sendMessage(Component.join(sep, components));
  }

  private void onCreate(GaiaUser user, String name) {
    if (!user.isPlayer()) {
      Message.PLAYER_REQUIRED.send(user);
      return;
    }
    new UserArenaFactory(user).tryCreate(name);
  }

  private void onRemove(GaiaUser user, Arena arena) {
    String arenaName = arena.name();
    if (user.parent().arenaService().remove(arenaName)) {
      Message.REMOVE_SUCCESS.send(user, arenaName);
    } else {
      Message.REMOVE_FAIL.send(user, arenaName);
    }
  }

  private void onRevert(GaiaUser user, Arena arena) {
    if (user.isPlayer() && !hasBypass(user)) {
      long cooldown = ConfigManager.instance().config().cooldown();
      long deltaTime = arena.lastReverted() + cooldown - System.currentTimeMillis();
      if (deltaTime > 0) {
        Message.REVERT_COOLDOWN.send(user, deltaTime);
        return;
      }
    }
    var revertResult = user.parent().arenaService().revert(arena);
    user.sendMessage(revertResult.message());
    revertResult.future().whenComplete((result, e) -> {
      if (e == null) {
        if (result.isPresent()) {
          user.sendMessage(Message.FINISHED_REVERT.build(arena.displayName(), result.getAsLong()));
        } else {
          user.sendMessage(Message.REVERT_ERROR_UNKNOWN.build(arena.displayName()));
        }
      } else if (e instanceof CancellationException) {
        user.sendMessage(Message.CANCEL_SUCCESS.build(arena.displayName()));
      } else {
        commander().logger().error(e.getMessage(), e);
      }
    });
  }

  private boolean hasBypass(GaiaUser user) {
    return user.get(PermissionChecker.POINTER).map(c -> c.test(CommandPermissions.BYPASS.toString())).orElse(false);
  }

  private void onCancel(GaiaUser user, Arena arena) {
    if (arena.reverting()) {
      user.parent().arenaService().cancelRevert(arena);
    } else {
      Message.CANCEL_FAIL.send(user, arena.displayName());
    }
  }

  private static Component generatePaging(boolean forward, int page) {
    return Component.text(forward ? " >>>" : "<<< ", NamedTextColor.GOLD)
      .hoverEvent(HoverEvent.showText(Component.text("Click to navigate to page " + page, NamedTextColor.GRAY)))
      .clickEvent(ClickEvent.runCommand("/gaia list " + page));
  }
}
