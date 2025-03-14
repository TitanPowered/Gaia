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

package me.moros.gaia.fabric;

import java.nio.file.Path;

import me.moros.gaia.api.platform.GaiaUser;
import me.moros.gaia.api.service.LevelService;
import me.moros.gaia.api.service.SelectionService;
import me.moros.gaia.api.service.UserService;
import me.moros.gaia.api.util.PluginInfo;
import me.moros.gaia.common.AbstractGaia;
import me.moros.gaia.common.command.Commander;
import me.moros.gaia.common.util.PluginInfoContainer;
import me.moros.gaia.fabric.platform.FabricGaiaUser;
import me.moros.gaia.fabric.service.FabricWorldEditSelectionService;
import me.moros.gaia.fabric.service.GaiaSelectionService;
import me.moros.gaia.fabric.service.LevelServiceImpl;
import me.moros.gaia.fabric.service.UserServiceImpl;
import me.moros.tasker.executor.SyncExecutor;
import me.moros.tasker.fabric.FabricExecutor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.server.MinecraftServer;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.slf4j.LoggerFactory;

public class FabricGaia extends AbstractGaia<ModContainer> {
  private final Commander commander;

  FabricGaia(ModContainer container, Path path) {
    super(container, path, LoggerFactory.getLogger(container.getMetadata().getName()));
    registerLifecycleListeners();
    CommandManager<GaiaUser> manager = new FabricServerCommandManager<>(
      ExecutionCoordinator.simpleCoordinator(),
      SenderMapper.create(s -> FabricGaiaUser.from(api(), s), s -> ((FabricGaiaUser) s).handle())
    );
    commander = Commander.create(manager, api(), logger());
  }

  private void registerLifecycleListeners() {
    ServerLifecycleEvents.SERVER_STARTED.register(this::onEnable);
    ServerLifecycleEvents.SERVER_STOPPING.register(this::onDisable);
  }

  private void onEnable(MinecraftServer server) {
    factory
      .bind(PluginInfo.class, this::createInfo)
      .bind(SyncExecutor.class, FabricExecutor::new)
      .bind(UserService.class, () -> new UserServiceImpl(this::api, server.getPlayerList()))
      .bind(LevelService.class, () -> new LevelServiceImpl(logger(), server));
    bindSelectionService(server);
    load();
  }

  private void onDisable(MinecraftServer server) {
    disable();
  }

  private void bindSelectionService(MinecraftServer server) {
    if (FabricLoader.getInstance().isModLoaded("worldedit")) {
      factory.bind(SelectionService.class, () -> new FabricWorldEditSelectionService(server));
    } else {
      factory.bind(SelectionService.class, GaiaSelectionService::new);
    }
  }

  private PluginInfo createInfo() {
    return new PluginInfoContainer(
      parent.getMetadata().getAuthors().stream().map(Person::getName).findFirst().orElse("Moros"),
      parent.getMetadata().getVersion().getFriendlyString()
    );
  }
}
