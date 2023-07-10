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
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
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
  private static final long DELAY = 500;

  private final Logger logger;
  private final Path translationsDirectory;
  private final AtomicLong lastUpdate;
  private final AtomicReference<ForwardingTranslationRegistry> registryReference;
  private final WatchServiceListener listener;

  public TranslationManager(Logger logger, Path directory) {
    this.logger = logger;
    this.lastUpdate = new AtomicLong();
    try {
      this.translationsDirectory = Files.createDirectories(directory.resolve("translations"));
      var registry = createRegistry();
      this.registryReference = new AtomicReference<>(registry);
      GlobalTranslator.translator().addSource(registry);
      this.listener = WatchServiceListener.create();
      this.listener.listenToDirectory(translationsDirectory, e -> reload());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void reload() {
    long time = System.currentTimeMillis();
    long previous = lastUpdate.getAndSet(time);
    if (time < previous + DELAY) {
      return;
    }
    var newRegistry = createRegistry();
    var old = registryReference.getAndSet(newRegistry);
    GlobalTranslator.translator().removeSource(old);
    GlobalTranslator.translator().addSource(newRegistry);
    int amount = newRegistry.locales().size();
    if (amount > 0) {
      String translations = newRegistry.locales().stream().map(Locale::getLanguage)
        .collect(Collectors.joining(", ", "[", "]"));
      logger.info(String.format("Loaded %d translations: %s", amount, translations));
    }
  }

  private ForwardingTranslationRegistry createRegistry() {
    var registry = new ForwardingTranslationRegistry(Key.key("gaia", "translations"));
    registry.defaultLocale(DEFAULT_LOCALE);
    loadCustom(registry);
    loadDefaults(registry);
    return registry;
  }

  private void loadDefaults(TranslationRegistry registry) {
    ResourceBundle bundle = ResourceBundle.getBundle(PATH, DEFAULT_LOCALE, UTF8ResourceBundleControl.get());
    registry.registerAll(DEFAULT_LOCALE, bundle, false);
  }

  private void loadCustom(TranslationRegistry registry) {
    Collection<Path> files;
    try (Stream<Path> stream = Files.list(translationsDirectory)) {
      files = stream.filter(this::isValidPropertyFile).toList();
    } catch (IOException e) {
      files = List.of();
    }
    files.forEach(f -> loadTranslationFile(f, registry));
  }

  private void loadTranslationFile(Path path, TranslationRegistry registry) {
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
  }

  private boolean isValidPropertyFile(Path path) {
    return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".properties");
  }

  private String removeFileExtension(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(0, fileName.length() - ".properties".length());
  }

  public void close() {
    try {
      listener.close();
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }
  }
}
