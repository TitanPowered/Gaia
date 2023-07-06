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

package me.moros.gaia.common.locale;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import org.spongepowered.configurate.reference.WatchServiceListener;

public class TranslationManager {
  private static final String PATH = "gaia.lang.messages_en";
  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
  private static final Object lock = new Object();

  private final Logger logger;
  private final Path translationsDirectory;
  private final WatchServiceListener listener;
  private final Set<Locale> installed;
  private TranslationRegistry registry;

  public TranslationManager(Logger logger, Path directory) {
    this.logger = logger;
    this.installed = ConcurrentHashMap.newKeySet();
    try {
      this.translationsDirectory = Files.createDirectories(directory.resolve("translations"));
      this.listener = WatchServiceListener.create();
      this.listener.listenToDirectory(translationsDirectory, e -> reload());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    reload();
  }

  private void reload() {
    var newRegistry = TranslationRegistry.create(Key.key("gaia", "translations"));
    newRegistry.defaultLocale(DEFAULT_LOCALE);
    var wrappedRegistry = loadCustom(logger, translationsDirectory, new Registry(newRegistry, new HashSet<>()));
    ResourceBundle bundle = ResourceBundle.getBundle(PATH, DEFAULT_LOCALE, UTF8ResourceBundleControl.get());
    wrappedRegistry.registry().registerAll(DEFAULT_LOCALE, bundle, false);

    synchronized (lock) {
      if (registry != null) {
        GlobalTranslator.translator().removeSource(registry);
      }
      registry = wrappedRegistry.registry();
      GlobalTranslator.translator().addSource(registry);
      installed.clear();
      installed.addAll(wrappedRegistry.locales());
    }
  }

  public void close() {
    try {
      listener.close();
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  private static Registry loadCustom(Logger logger, Path dir, Registry wrappedRegistry) {
    Collection<Path> files;
    try (Stream<Path> stream = Files.list(dir)) {
      files = stream.filter(TranslationManager::isValidPropertyFile).collect(Collectors.toList());
    } catch (IOException e) {
      files = Collections.emptyList();
    }
    files.forEach(path -> loadTranslationFile(logger, wrappedRegistry, path));
    int amount = wrappedRegistry.locales().size();
    if (amount > 0) {
      String translations = wrappedRegistry.locales().stream().map(Locale::getLanguage)
        .collect(Collectors.joining(", ", "[", "]"));
      logger.info(String.format("Loaded %d translations: %s", amount, translations));
    }
    return wrappedRegistry;
  }

  private static void loadTranslationFile(Logger logger, Registry wrappedRegistry, Path path) {
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
    wrappedRegistry.registry().registerAll(locale, bundle, false);
    wrappedRegistry.locales().add(locale);
  }

  private static boolean isValidPropertyFile(Path path) {
    return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".properties");
  }

  private static String removeFileExtension(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(0, fileName.length() - ".properties".length());
  }

  private record Registry(TranslationRegistry registry, Set<Locale> locales) {
  }
}
