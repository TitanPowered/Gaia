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

package me.moros.gaia.common.service;

import java.util.concurrent.Executors;

import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.event.EventBus;
import me.moros.gaia.api.service.ArenaService;
import me.moros.gaia.api.service.Coordinator;
import me.moros.gaia.api.service.LevelService;
import me.moros.gaia.api.service.OperationService;
import me.moros.gaia.api.service.SelectionService;
import me.moros.gaia.api.service.UserService;
import me.moros.gaia.api.storage.Storage;
import me.moros.gaia.common.GaiaFactory;
import me.moros.gaia.common.event.EventBusImpl;
import me.moros.gaia.common.storage.FileStorage;
import me.moros.gaia.common.storage.decoder.Decoder;
import me.moros.tasker.executor.CompositeExecutor;
import me.moros.tasker.executor.SimpleAsyncExecutor;
import me.moros.tasker.executor.SyncExecutor;

public class CoordinatorImpl implements Coordinator {
  private final Gaia plugin;
  private final CompositeExecutor executor;
  private final Storage storage;
  private final EventBus eventBus;
  private final UserService userService;
  private final SelectionService selectionService;
  private final LevelService levelService;
  private final OperationService operationService;
  private final ArenaService arenaService;

  public CoordinatorImpl(Gaia plugin, GaiaFactory factory) {
    this.plugin = plugin;
    var threads = calculateThreads(plugin.configManager().config().backgroundThreads());
    var pool = Executors.newScheduledThreadPool(threads);
    this.executor = CompositeExecutor.of(factory.build(SyncExecutor.class), new SimpleAsyncExecutor(pool));
    this.storage = FileStorage.createInstance(plugin, factory.build(Decoder.class));
    this.eventBus = new EventBusImpl();
    this.userService = factory.build(UserService.class);
    this.selectionService = factory.build(SelectionService.class);
    this.levelService = factory.build(LevelService.class);
    this.operationService = new OperationServiceImpl(plugin, executor.sync());
    this.arenaService = new ArenaServiceImpl(plugin);
  }

  @Override
  public CompositeExecutor executor() {
    return executor;
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
  public void shutdown() {
    plugin.configManager().close();
    operationService.shutdown();
    eventBus.shutdown();
    executor.shutdown();
  }

  private int calculateThreads(int threads) {
    if (threads < 1) {
      threads = Runtime.getRuntime().availableProcessors() / 2;
    }
    return Math.max(1, threads);
  }
}
