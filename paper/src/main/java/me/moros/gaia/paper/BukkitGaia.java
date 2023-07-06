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

package me.moros.gaia.paper;

import java.nio.file.Path;
import java.util.stream.Collectors;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.permission.CommandPermission;
import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.api.service.LevelService;
import me.moros.gaia.api.service.SelectionService;
import me.moros.gaia.api.service.UserService;
import me.moros.gaia.api.util.PluginInfo;
import me.moros.gaia.common.AbstractGaia;
import me.moros.gaia.common.command.CommandPermissions;
import me.moros.gaia.common.command.Commander;
import me.moros.gaia.common.util.PluginInfoContainer;
import me.moros.gaia.paper.platform.BukkitGaiaUser;
import me.moros.gaia.paper.service.BukkitWorldEditSelectionService;
import me.moros.gaia.paper.service.GaiaSelectionService;
import me.moros.gaia.paper.service.LevelServiceImpl;
import me.moros.gaia.paper.service.UserServiceImpl;
import me.moros.tasker.bukkit.BukkitExecutor;
import me.moros.tasker.executor.SyncExecutor;
import org.bstats.bukkit.Metrics;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.ServicePriority;
import org.slf4j.Logger;

public class BukkitGaia extends AbstractGaia<GaiaBootstrap> {
  private Commander commander;

  protected BukkitGaia(GaiaBootstrap parent, Path path, Logger logger) {
    super(parent, path, logger);
  }

  void onPluginEnable() {
    new Metrics(parent, 8608);
    factory
      .bind(PluginInfo.class, this::createInfo)
      .bind(SyncExecutor.class, () -> new BukkitExecutor(parent))
      .bind(UserService.class, () -> new UserServiceImpl(api(), parent.getServer()))
      .bind(LevelService.class, () -> new LevelServiceImpl(logger()));
    bindSelectionService();
    load();

    try {
      PaperCommandManager<GaiaUser> manager = new PaperCommandManager<>(parent,
        CommandExecutionCoordinator.simpleCoordinator(),
        c -> BukkitGaiaUser.from(api(), c),
        u -> ((BukkitGaiaUser) u).handle()
      );
      manager.registerAsynchronousCompletions();
      initPermissions();
      commander = Commander.create(manager, api(), logger());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    parent.getServer().getServicesManager().register(Gaia.class, api(), parent, ServicePriority.Normal);
  }

  void onPluginDisable() {
    disable();
  }

  private void bindSelectionService() {
    if (parent.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
      factory.bind(SelectionService.class, BukkitWorldEditSelectionService::new);
    } else {
      factory.bind(SelectionService.class, () -> new GaiaSelectionService(parent));
    }
  }

  private void initPermissions() {
    var adminPerms = CommandPermissions.adminOnly().collect(Collectors.toMap(CommandPermission::toString, p -> true));
    parent.getServer().getPluginManager().addPermission(new Permission(CommandPermissions.VERSION.toString(), PermissionDefault.TRUE));
    parent.getServer().getPluginManager().addPermission(new Permission("gaia.admin", PermissionDefault.OP, adminPerms));
  }

  private PluginInfo createInfo() {
    return new PluginInfoContainer(parent.getPluginMeta().getAuthors().get(0), parent.getPluginMeta().getVersion());
  }
}
