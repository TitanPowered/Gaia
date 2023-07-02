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

package me.moros.gaia.api.service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import me.moros.gaia.api.region.Selection;
import me.moros.gaia.api.user.GaiaUser;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface UserService {
  @Nullable GaiaUser findUser(UUID uuid);

  @Nullable GaiaUser findUser(String input);

  Optional<Selection> selection(@Nullable UUID uuid);

  Stream<String> users();
}
