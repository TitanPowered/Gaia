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

package me.moros.gaia.locale;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.slf4j.Logger;

public class TranslationManager {
  public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  private final Set<Locale> installed = ConcurrentHashMap.newKeySet();
  private final Path translationsDirectory;
  private TranslationRegistry registry;
  private final Logger logger;

  public TranslationManager(Logger logger, String directory) {
    this.logger = logger;
    translationsDirectory = Paths.get(directory, "translations");
    reload();
  }

  public void reload() {
    if (registry != null) {
      GlobalTranslator.translator().removeSource(registry);
      installed.clear();
    }
    registry = TranslationRegistry.create(Key.key("gaia", "translations"));
    registry.defaultLocale(DEFAULT_LOCALE);

    loadCustom();

    ResourceBundle bundle = ResourceBundle.getBundle("gaia", DEFAULT_LOCALE, UTF8ResourceBundleControl.get());
    registry.registerAll(DEFAULT_LOCALE, bundle, false);
    GlobalTranslator.translator().addSource(registry);
  }

  private void loadCustom() {
    Collection<Path> files;
    try (Stream<Path> stream = Files.list(translationsDirectory)) {
      files = stream.filter(this::isValidPropertyFile).collect(Collectors.toList());
    } catch (IOException e) {
      files = Collections.emptyList();
    }
    files.forEach(this::loadTranslationFile);
    int amount = installed.size();
    if (amount > 0) {
      String translations = installed.stream().map(Locale::getLanguage).collect(Collectors.joining(", ", "[", "]"));
      logger.info("Loaded " + amount + " translations: " + translations);
    }
  }

  private void loadTranslationFile(Path path) {
    String localeString = removeFileExtension(path);
    Locale locale = Translator.parseLocale(localeString);
    if (locale == null) {
      logger.warn("Unknown locale: " + localeString);
      return;
    }
    PropertyResourceBundle bundle;
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      bundle = new PropertyResourceBundle(reader);
    } catch (IOException e) {
      logger.warn("Error loading locale file: " + localeString);
      return;
    }
    registry.registerAll(locale, bundle, false);
    installed.add(locale);
  }

  private boolean isValidPropertyFile(Path path) {
    return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".properties");
  }

  private String removeFileExtension(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(0, fileName.length() - ".properties".length());
  }
}
