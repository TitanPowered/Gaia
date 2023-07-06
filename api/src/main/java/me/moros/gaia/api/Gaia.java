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

package me.moros.gaia.api;

import me.moros.gaia.api.event.EventBus;
import me.moros.gaia.api.service.ArenaService;
import me.moros.gaia.api.service.LevelService;
import me.moros.gaia.api.service.OperationService;
import me.moros.gaia.api.service.SelectionService;
import me.moros.gaia.api.service.UserService;
import me.moros.gaia.api.storage.Storage;
import me.moros.gaia.api.util.PluginInfo;
import me.moros.tasker.executor.CompositeExecutor;

public interface Gaia {
  CompositeExecutor executor();

  Storage storage();

  EventBus eventBus();

  UserService userService();

  SelectionService selectionService();

  LevelService levelService();

  OperationService operationService();

  ArenaService arenaService();

  PluginInfo pluginInfo();
}
