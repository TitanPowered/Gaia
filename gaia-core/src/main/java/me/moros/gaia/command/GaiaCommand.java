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

package me.moros.gaia.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;

import cloud.commandframework.Command.Builder;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.StringArgument.StringMode;
import cloud.commandframework.meta.CommandMeta;
import me.moros.gaia.GaiaPlugin;
import me.moros.gaia.api.Arena;
import me.moros.gaia.api.ArenaPoint;
import me.moros.gaia.api.GaiaUser;
import me.moros.gaia.io.GaiaIO;
import me.moros.gaia.locale.Message;
import me.moros.gaia.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class GaiaCommand {
  private static final int AMOUNT_PER_PAGE = 12;

  private final GaiaPlugin plugin;
  private final CommandManager<GaiaUser> manager;
  private final Builder<GaiaUser> builder;

  public GaiaCommand(GaiaPlugin plugin, CommandManager<GaiaUser> manager) {
    this.plugin = plugin;
    this.manager = manager;
    this.builder = manager.commandBuilder("gaia", "g", "arena", "arenas")
      .meta(CommandMeta.DESCRIPTION, "Base command for Gaia");
    construct();
  }

  private void construct() {
    var arenaArg = manager.argumentBuilder(Arena.class, "arena")
      .asOptionalWithDefault("cur");
    var targetArg = manager.argumentBuilder(GaiaUser.class, "target")
      .asOptionalWithDefault("me");

    //noinspection ConstantConditions
    manager
      .command(builder.handler(c -> plugin.queryCommands("", c.getSender())))
      .command(builder.literal("version", "v")
        .meta(CommandMeta.DESCRIPTION, "View version info about the plugin")
        .permission(CommandPermissions.VERSION)
        .handler(c -> onVersion(c.getSender()))
      ).command(builder.literal("reload", "rel")
        .meta(CommandMeta.DESCRIPTION, "Reload the plugin and its config")
        .permission(CommandPermissions.RELOAD)
        .handler(c -> onReload(c.getSender()))
      ).command(builder.literal("list", "ls")
        .meta(CommandMeta.DESCRIPTION, "List all Gaia arenas")
        .permission(CommandPermissions.LIST)
        .argument(IntegerArgument.optional("page", 1))
        .handler(c -> onList(c.getSender(), c.get("page")))
      ).command(builder.literal("info", "i")
        .meta(CommandMeta.DESCRIPTION, "View info about the specified arena")
        .permission(CommandPermissions.INFO)
        .argument(arenaArg.build())
        .handler(c -> onInfo(c.getSender(), c.get("arena")))
      ).command(builder.literal("create", "c")
        .meta(CommandMeta.DESCRIPTION, "Create a Gaia arena")
        .permission(CommandPermissions.CREATE)
        .argument(StringArgument.of("name", StringMode.SINGLE))
        .handler(c -> onCreate(c.getSender(), c.get("name")))
      ).command(builder.literal("remove", "rm")
        .meta(CommandMeta.DESCRIPTION, "Remove a Gaia arena")
        .permission(CommandPermissions.REMOVE)
        .argument(arenaArg.build())
        .handler(c -> onRemove(c.getSender(), c.get("arena")))
      ).command(builder.literal("revert", "reset")
        .meta(CommandMeta.DESCRIPTION, "Revert the specified arena")
        .permission(CommandPermissions.REVERT)
        .argument(arenaArg.build())
        .argument(targetArg.build())
        .handler(c -> onRevert(c.get("target"), c.get("arena")))
      ).command(builder.literal("cancel", "abort")
        .meta(CommandMeta.DESCRIPTION, "Cancel reverting the specified arena")
        .permission(CommandPermissions.CANCEL)
        .argument(arenaArg.build())
        .handler(c -> onCancel(c.getSender(), c.get("arena")))
      ).command(builder.literal("help", "h")
        .meta(CommandMeta.DESCRIPTION, "View info about Gaia commands")
        .permission(CommandPermissions.HELP)
        .argument(StringArgument.optional("query", StringMode.GREEDY))
        .handler(c -> plugin.queryCommands(c.getOrDefault("query", ""), c.getSender()))
      ).command(builder.literal("addpoint", "add", "a")
        .meta(CommandMeta.DESCRIPTION, "Add a new point")
        .permission(CommandPermissions.POINT)
        .handler(c -> onPointAdd(c.getSender()))
      ).command(builder.literal("clearpoints", "clearpoint", "clear")
        .meta(CommandMeta.DESCRIPTION, "Clear all points for the specified arena")
        .permission(CommandPermissions.POINT)
        .argument(arenaArg.build())
        .handler(c -> onPointClear(c.getSender(), c.get("arena")))
      ).command(builder.literal("teleport", "tp")
        .meta(CommandMeta.DESCRIPTION, "Teleport to a point in the specified arena")
        .permission(CommandPermissions.TELEPORT)
        .argument(arenaArg.build())
        .argument(IntegerArgument.optional("id", 0))
        .handler(c -> onPointTeleport(c.getSender(), c.get("arena"), c.get("id")))
      );
  }

  private void onVersion(GaiaUser user) {
    String link = "https://github.com/PrimordialMoros/Gaia";
    Component version = Message.brand(Component.text("Version: ", NamedTextColor.DARK_AQUA))
      .append(Component.text(plugin.version(), NamedTextColor.GREEN))
      .hoverEvent(HoverEvent.showText(Message.VERSION_COMMAND_HOVER.build(plugin.author(), link)))
      .clickEvent(ClickEvent.openUrl(link));
    user.sendMessage(version);
  }

  private void onReload(GaiaUser user) {
    plugin.reload();
    Message.CONFIG_RELOAD.send(user);
  }

  private void onList(GaiaUser user, Integer page) {
    int count = plugin.arenaManager().size();
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
    plugin.arenaManager().stream().sorted(Comparator.comparing(Arena::name)).
      skip(skip).limit(AMOUNT_PER_PAGE).
      map(Arena::info).forEach(user::sendMessage);
    user.sendMessage(Component.text(Util.generateLine(44), NamedTextColor.DARK_AQUA));
  }

  private void onInfo(GaiaUser user, Arena arena) {
    user.sendMessage(arena.info());
    List<ArenaPoint> points = arena.points();
    if (points.isEmpty()) {
      Message.NO_POINTS.send(user, arena.displayName());
      return;
    }
    ListIterator<ArenaPoint> it = points.listIterator();
    List<Component> components = new ArrayList<>();
    while (it.hasNext()) {
      ArenaPoint point = it.next();
      int index = it.nextIndex();
      components.add(Component.text()
        .append(Component.text("[", NamedTextColor.DARK_GRAY))
        .append(Component.text(index, NamedTextColor.DARK_AQUA))
        .append(Component.text("]", NamedTextColor.DARK_GRAY))
        .hoverEvent(HoverEvent.showText(point.details()))
        .clickEvent(ClickEvent.runCommand("/gaia teleport " + arena.name() + " " + index))
        .build());
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
    String arenaName = Util.sanitizeInput(name);
    if (arenaName.length() < 3) {
      Message.CREATE_ERROR_VALIDATION.send(user);
      return;
    }
    if (plugin.arenaManager().contains(arenaName)) {
      Message.CREATE_ERROR_EXISTS.send(user, arenaName);
      return;
    }
    plugin.arenaManager().create(user, arenaName);
  }

  private void onRemove(GaiaUser user, Arena arena) {
    String arenaName = arena.name();
    if (plugin.arenaManager().remove(arenaName)) {
      Message.REMOVE_SUCCESS.send(user, arenaName);
    } else {
      Message.REMOVE_FAIL.send(user, arenaName);
    }
  }

  private void onRevert(GaiaUser user, Arena arena) {
    if (user.isPlayer() && !user.hasPermission(CommandPermissions.BYPASS.toString())) {
      long deltaTime = plugin.arenaManager().nextRevertTime(arena) - System.currentTimeMillis();
      if (deltaTime > 0) {
        Message.REVERT_COOLDOWN.send(user, deltaTime);
        return;
      }
    }
    if (!arena.finalized()) {
      Message.REVERT_ERROR_ANALYZING.send(user, arena.displayName());
      return;
    }
    if (arena.reverting()) {
      Message.REVERT_ERROR_REVERTING.send(user, arena.displayName());
      return;
    }
    Message.REVERT_SUCCESS.send(user, arena.displayName());
    arena.resetLastReverted();
    plugin.arenaManager().revert(user, arena);
  }

  private void onCancel(GaiaUser user, Arena arena) {
    if (arena.reverting()) {
      plugin.arenaManager().cancelRevert(arena);
    } else {
      Message.CANCEL_FAIL.send(user, arena.displayName());
    }
  }

  private void onPointAdd(GaiaUser user) {
    Arena arena = plugin.arenaManager().standingArena(user);
    if (arena == null) {
      Message.ADD_POINT_FAIL_AREA.send(user);
      return;
    }
    ArenaPoint point = plugin.pointFromUser(user);
    if (point != null) {
      arena.addPoint(point);
      GaiaIO.instance().updateArenaPoints(arena);
      Message.ADD_POINT_SUCCESS.send(user);
    } else {
      Message.ADD_POINT_FAIL.send(user);
    }
  }

  private void onPointClear(GaiaUser user, Arena arena) {
    arena.clearPoints();
    GaiaIO.instance().updateArenaPoints(arena);
    Message.CLEAR_POINTS.send(user, arena.displayName());
  }

  private void onPointTeleport(GaiaUser user, Arena arena, Integer id) {
    List<ArenaPoint> points = arena.points();
    if (points.isEmpty()) {
      Message.NO_POINTS.send(user, arena.displayName());
      return;
    }
    if (id < 0 || id > points.size()) {
      Message.INVALID_POINT.send(user);
      return;
    }
    ArenaPoint pointToTeleport = points.get(id == 0 ? ThreadLocalRandom.current().nextInt(points.size()) : id - 1);
    plugin.teleport(user, arena.worldUID(), pointToTeleport);
  }

  private static Component generatePaging(boolean forward, int page) {
    return Component.text(forward ? " >>>" : "<<< ", NamedTextColor.GOLD)
      .hoverEvent(HoverEvent.showText(Component.text("Click to navigate to page " + page, NamedTextColor.GRAY)))
      .clickEvent(ClickEvent.runCommand("/gaia list " + page));
  }
}
