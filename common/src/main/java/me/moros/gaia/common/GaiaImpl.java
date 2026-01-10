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

package me.moros.gaia.common;

import java.util.concurrent.Executors;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.event.EventBus;
import me.moros.gaia.api.service.ArenaService;
import me.moros.gaia.api.service.LevelService;
import me.moros.gaia.api.service.OperationService;
import me.moros.gaia.api.service.SelectionService;
import me.moros.gaia.api.service.UserService;
import me.moros.gaia.api.storage.Storage;
import me.moros.gaia.api.util.PluginInfo;
import me.moros.gaia.common.event.EventBusImpl;
import me.moros.gaia.common.service.ArenaServiceImpl;
import me.moros.gaia.common.service.OperationServiceImpl;
import me.moros.gaia.common.service.RevertListener;
import me.moros.gaia.common.storage.FileStorage;
import me.moros.tasker.executor.CompositeExecutor;
import me.moros.tasker.executor.SimpleAsyncExecutor;
import me.moros.tasker.executor.SyncExecutor;

final class GaiaImpl implements Gaia {
  private final PluginInfo info;
  private final CompositeExecutor executor;
  private final Storage storage;
  private final EventBus eventBus;
  private final UserService userService;
  private final SelectionService selectionService;
  private final LevelService levelService;
  private final OperationService operationService;
  private final ArenaService arenaService;

  GaiaImpl(AbstractGaia<?> plugin, GaiaFactory factory) {
    this.info = factory.build(PluginInfo.class);
    this.executor = CompositeExecutor.of(
      factory.build(SyncExecutor.class),
      new SimpleAsyncExecutor(Executors.newVirtualThreadPerTaskExecutor())
    );
    this.storage = FileStorage.createInstance(executor.async(), plugin.logger(), plugin.path());
    this.eventBus = new EventBusImpl(plugin.logger());
    this.userService = factory.build(UserService.class);
    this.selectionService = factory.build(SelectionService.class);
    this.levelService = factory.build(LevelService.class);
    this.operationService = new OperationServiceImpl(this, executor.sync());
    this.arenaService = new ArenaServiceImpl(this);
    new RevertListener(this);
  }

  @Override
  public Storage storage() {
    return storage;
  }

  @Override
  public EventBus eventBus() {
    return eventBus;
  }

  @Override
  public UserService userService() {
    return userService;
  }

  @Override
  public SelectionService selectionService() {
    return selectionService;
  }

  @Override
  public LevelService levelService() {
    return levelService;
  }

  @Override
  public OperationService operationService() {
    return operationService;
  }

  @Override
  public ArenaService arenaService() {
    return arenaService;
  }

  @Override
  public PluginInfo pluginInfo() {
    return info;
  }

  void shutdown() {
    operationService.shutdown();
    eventBus.shutdown();
    executor.shutdown();
  }
}
