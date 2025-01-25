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

package me.moros.gaia.common.command.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.api.util.ComponentUtil;
import me.moros.gaia.api.util.TextUtil;
import me.moros.gaia.common.command.CommandPermissions;
import me.moros.gaia.common.command.Commander;
import me.moros.gaia.common.command.GaiaCommand;
import me.moros.gaia.common.command.parser.ArenaParser;
import me.moros.gaia.common.command.parser.GaiaUserParser;
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
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;

public record ArenaCommand(Commander commander) implements GaiaCommand {
  private static final int AMOUNT_PER_PAGE = 12;

  @Override
  public void register() {
    var builder = commander().rootBuilder();
    commander().register(builder
      .literal("list")
      .optional("page", IntegerParser.integerParser(1), DefaultValue.constant(1))
      .commandDescription(RichDescription.of(Message.LIST_CMD_DESC.build()))
      .permission(CommandPermissions.LIST)
      .handler(c -> onList(c.sender(), c.get("page")))
    );
    commander().register(builder
      .literal("info")
      .optional("arena", ArenaParser.parser(), DefaultValue.parsed("cur"))
      .commandDescription(RichDescription.of(Message.INFO_CMD_DESC.build()))
      .permission(CommandPermissions.INFO)
      .handler(c -> onInfo(c.sender(), c.get("arena")))
    );
    commander().register(builder
      .literal("create")
      .optional("name", StringParser.stringParser())
      .commandDescription(RichDescription.of(Message.CREATE_CMD_DESC.build()))
      .permission(CommandPermissions.CREATE)
      .handler(c -> onCreate(c.sender(), c.get("name")))
    );
    commander().register(builder
      .literal("remove")
      .required("arena", ArenaParser.parser())
      .commandDescription(RichDescription.of(Message.REMOVE_CMD_DESC.build()))
      .permission(CommandPermissions.REMOVE)
      .handler(c -> onRemove(c.sender(), c.get("arena")))
    );
    commander().register(builder
      .literal("revert")
      .optional("arena", ArenaParser.parser(), DefaultValue.parsed("cur"))
      .optional("target", GaiaUserParser.parser(), DefaultValue.parsed("me"))
      .commandDescription(RichDescription.of(Message.REVERT_CMD_DESC.build()))
      .permission(CommandPermissions.REVERT)
      .handler(c -> onRevert(c.get("target"), c.get("arena")))
    );
  }

  private void onList(GaiaUser user, int page) {
    int count = user.parent().arenaService().size();
    if (count == 0) {
      Message.LIST_NOT_FOUND.send(user);
      return;
    }
    int totalPages = (int) Math.ceil(count / (double) AMOUNT_PER_PAGE);
    if (page > totalPages) {
      Message.LIST_INVALID_PAGE.send(user, totalPages);
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
    new UserArenaFactory(user, commander().logger()).tryCreate(name);
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
        user.sendMessage(Message.FINISHED_REVERT.build(arena.displayName(), result));
      } else {
        user.sendMessage(Message.REVERT_ERROR_UNKNOWN.build(arena.displayName()));
        commander().logger().error(e.getMessage(), e);
      }
    });
  }

  private boolean hasBypass(GaiaUser user) {
    return user.get(PermissionChecker.POINTER).map(c -> c.test(CommandPermissions.BYPASS.toString())).orElse(false);
  }

  private static Component generatePaging(boolean forward, int page) {
    return Component.text(forward ? " >>>" : "<<< ", NamedTextColor.GOLD)
      .hoverEvent(HoverEvent.showText(Component.text("Click to navigate to page " + page, NamedTextColor.GRAY)))
      .clickEvent(ClickEvent.runCommand("/gaia list " + page));
  }
}
