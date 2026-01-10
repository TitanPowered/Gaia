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

package me.moros.gaia.common.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.zip.CRC32C;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.api.storage.Storage;
import me.moros.gaia.common.storage.serializer.Serializers;
import me.moros.tasker.executor.AsyncExecutor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.slf4j.Logger;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;

public final class FileStorage implements Storage {
  public static final Supplier<Checksum> ALGORITHM = CRC32C::new;

  private static final String ARENA_DIR = "arenas";
  private static final String ARENA_META = "meta.json";
  private static final String CHUNK_NAME_FORMAT = "c.%d.%d.schem";

  private final AsyncExecutor executor;
  private final Logger logger;
  private final Path container;
  private final Semaphore semaphore;

  private FileStorage(AsyncExecutor executor, Logger logger, Path container) {
    this.executor = executor;
    this.logger = logger;
    this.container = container;
    this.semaphore = new Semaphore(Math.max(Runtime.getRuntime().availableProcessors(), 2));
  }

  public static Storage createInstance(AsyncExecutor executor, Logger logger, Path path) {
    Objects.requireNonNull(executor);
    Objects.requireNonNull(logger);
    Objects.requireNonNull(path);
    Path container;
    try {
      container = Files.createDirectories(path.resolve(ARENA_DIR));
    } catch (IOException e) {
      throw new RuntimeException(String.format("Could not create %s directory! Aborting loading.", ARENA_DIR), e);
    }
    return new FileStorage(executor, logger, container);
  }

  private Path arenaPath(String name) {
    return container.resolve(name);
  }

  private Path arenaMeta(String name) {
    return arenaPath(name).resolve(ARENA_META);
  }

  private Path chunkPath(String name, ChunkPosition position) {
    return arenaPath(name).resolve(String.format(CHUNK_NAME_FORMAT, position.x(), position.z()));
  }

  @Override
  public boolean arenaFileExists(String name) {
    return Files.exists(arenaMeta(name));
  }

  @Override
  public boolean createEmptyArenaFiles(String name) {
    try {
      Path arenaMeta = arenaMeta(name);
      Files.createDirectories(arenaMeta.getParent());
      Files.createFile(arenaMeta);
      return true;
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return false;
  }

  @Override
  public boolean deleteArena(String name) {
    try (var stream = Files.walk(arenaPath(name), 1)) {
      stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      return true;
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return false;
  }

  private boolean isMeta(Path path) {
    return Files.isRegularFile(path) && path.getFileName().toString().equals(ARENA_META);
  }

  @Override
  public CompletableFuture<Iterable<Arena>> loadAllArenas() {
    return executor.submit(() -> {
      Iterable<Arena> arenas;
      try (Stream<Path> stream = Files.walk(container, 2)) {
        arenas = stream.filter(this::isMeta).map(this::loadArena).filter(Objects::nonNull).toList();
      } catch (IOException e) {
        throw new CompletionException(e);
      }
      return arenas;
    }).exceptionally(t -> {
      logger.error(t.getMessage(), t);
      return List.of();
    });
  }

  @Override
  public CompletableFuture<Arena> saveArena(Arena arena) {
    return executor.submit(() -> {
      try (var ref = load(arenaMeta(arena.name()))) {
        ref.set(NodePath.path(), arena);
        ref.save();
        return arena;
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    }).exceptionally(e -> {
      logger.error(e.getMessage(), e);
      return null;
    });
  }

  private Snapshot loadData(String name, ChunkRegion.Validated chunkRegion) throws IOException {
    var checksum = ALGORITHM.get();
    var path = chunkPath(name, chunkRegion);
    semaphore.acquireUninterruptibly();
    try (var fis = Files.newInputStream(path);
         var cis = new CheckedInputStream(fis, checksum);
         var bis = new BufferedInputStream(cis);
         var gis = new GZIPInputStream(bis);
         var dis = new DataInputStream(gis);
         var reader = new SchemReader(LinBinaryIO.read(dis))) {
      var data = reader.read(chunkRegion);
      validateChecksum(path, chunkRegion.checksum(), checksum.getValue());
      return data;
    } finally {
      semaphore.release();
    }
  }

  @Override
  public CompletableFuture<Collection<Snapshot>> loadDataAsync(String name, Collection<ChunkRegion.Validated> chunkRegions) {
    return executor.submit(() -> {
      Collection<Snapshot> result = new ArrayList<>();
      try {
        for (var chunkRegion : chunkRegions) {
          result.add(loadData(name, chunkRegion));
        }
        return result;
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    }).exceptionally(e -> {
      logger.error(e.getMessage(), e);
      return List.of();
    });
  }

  private long saveData(String name, Snapshot snapshot) throws IOException {
    var checksum = ALGORITHM.get();
    try (var fos = Files.newOutputStream(chunkPath(name, snapshot));
         var cos = new CheckedOutputStream(fos, checksum);
         var bos = new BufferedOutputStream(cos);
         var gos = new GZIPOutputStream(bos);
         var dos = new DataOutputStream(gos);
         var writer = new SchemWriter(dos)) {
      writer.write(snapshot);
    }
    return checksum.getValue();
  }

  @Override
  public CompletableFuture<Collection<ChunkRegion.Validated>> saveDataAsync(String name, Iterable<Snapshot> data) {
    return executor.submit(() -> {
      try {
        Collection<ChunkRegion.Validated> result = new ArrayList<>();
        for (var cd : data) {
          result.add(ChunkRegion.create(cd.chunk().region(), saveData(name, cd)));
        }
        return result;
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    }).exceptionally(e -> {
      logger.error(e.getMessage(), e);
      return List.of();
    });
  }

  private @Nullable Arena loadArena(Path path) {
    Arena arena = null;
    try (var ref = load(path)) {
      arena = Objects.requireNonNull(ref.node().get(Arena.class));
      for (var chunk : arena.chunks()) {
        var chunkPath = chunkPath(arena.name(), chunk);
        validateChecksum(chunkPath, chunk.checksum(), calculateChecksum(chunkPath));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return arena;
  }

  private void validateChecksum(Path path, long expected, long provided) throws ChecksumMismatchException {
    if (expected != provided) {
      throw new ChecksumMismatchException(path.toString(), expected, provided);
    }
  }

  private long calculateChecksum(Path filePath) throws IOException {
    Checksum checksum = ALGORITHM.get();
    try (var fis = Files.newInputStream(filePath);
         var cis = new CheckedInputStream(fis, checksum)) {
      byte[] buffer = new byte[8192];
      while (cis.read(buffer) >= 0) {
      }
      return checksum.getValue();
    }
  }

  private ConfigurationReference<BasicConfigurationNode> load(Path path) throws ConfigurateException {
    return GsonConfigurationLoader.builder()
      .defaultOptions(o -> o.serializers(b -> b.registerAll(Serializers.ALL)))
      .path(path)
      .build()
      .loadToReference();
  }
}
