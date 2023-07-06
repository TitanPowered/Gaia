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

package me.moros.gaia.common.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.zip.CRC32C;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import me.moros.gaia.api.Gaia;
import me.moros.gaia.api.arena.Arena;
import me.moros.gaia.api.arena.Point;
import me.moros.gaia.api.arena.region.ChunkRegion;
import me.moros.gaia.api.chunk.ChunkPosition;
import me.moros.gaia.api.chunk.Snapshot;
import me.moros.gaia.api.storage.Storage;
import me.moros.gaia.common.storage.adapter.Adapters;
import me.moros.gaia.common.storage.decoder.Decoder;
import me.moros.math.Vector3i;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.enginehub.linbus.stream.LinBinaryIO;

public final class FileStorage implements Storage {
  public static final Supplier<Checksum> ALGORITHM = CRC32C::new;

  private static final String ARENA_DIR = "arenas";
  private static final String ARENA_META = "meta.json";
  private static final String CHUNK_NAME_FORMAT = "c.%d.%d.schem";

  private final Gaia plugin;
  private final Path container;
  private final Decoder decoder;
  private final Gson gson;

  private FileStorage(Gaia plugin, Path container, Decoder decoder) {
    this.plugin = plugin;
    this.container = container;
    this.decoder = decoder;
    this.gson = new GsonBuilder().setPrettyPrinting()
      .registerTypeHierarchyAdapter(Vector3i.class, Adapters.VECTOR)
      .registerTypeHierarchyAdapter(Point.class, Adapters.POINT)
      .registerTypeHierarchyAdapter(ChunkRegion.Validated.class, Adapters.CHUNK)
      .registerTypeHierarchyAdapter(Arena.class, Adapters.ARENA)
      .create();
  }

  public static Storage createInstance(Gaia plugin, Decoder decoder) {
    Objects.requireNonNull(decoder);
    try {
      return new FileStorage(plugin, Files.createDirectories(plugin.path().resolve(ARENA_DIR)), decoder);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Could not create %s directory! Aborting loading.", ARENA_DIR), e);
    }
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
      plugin.logger().error(e.getMessage(), e);
    }
    return false;
  }

  @Override
  public boolean deleteArena(String name) {
    try (var stream = Files.walk(arenaPath(name), 1)) {
      stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      return true;
    } catch (IOException e) {
      plugin.logger().error(e.getMessage(), e);
    }
    return false;
  }

  private boolean isMeta(Path path) {
    return Files.isRegularFile(path) && path.getFileName().toString().equals(ARENA_META);
  }

  @Override
  public CompletableFuture<Iterable<Arena>> loadAllArenas() {
    return plugin.coordinator().executor().async().submit(() -> {
      Iterable<Arena> arenas;
      try (Stream<Path> stream = Files.walk(container, 2)) {
        arenas = stream.filter(this::isMeta).map(this::loadArena).filter(Objects::nonNull).toList();
      } catch (IOException e) {
        throw new CompletionException(e);
      }
      return arenas;
    }).exceptionally(t -> {
      plugin.logger().error(t.getMessage(), t);
      return List.of();
    });
  }

  @Override
  public CompletableFuture<Arena> saveArena(Arena arena) {
    return plugin.coordinator().executor().async().submit(() -> {
      Path arenaMeta = arenaMeta(arena.name());
      try (var writer = Files.newBufferedWriter(arenaMeta, StandardCharsets.UTF_8)) {
        gson.toJson(arena, writer);
        return arena;
      } catch (Exception e) {
        throw new CompletionException(e);
      }
    }).exceptionally(e -> {
      plugin.logger().error(e.getMessage(), e);
      return null;
    });
  }

  private Snapshot loadData(String name, ChunkRegion.Validated chunkRegion) throws IOException {
    var checksum = ALGORITHM.get();
    var path = chunkPath(name, chunkRegion);
    try (var fis = Files.newInputStream(path);
         var cis = new CheckedInputStream(fis, checksum);
         var bis = new BufferedInputStream(cis);
         var gis = new GZIPInputStream(bis);
         var dis = new DataInputStream(gis);
         var reader = new SchemReader(LinBinaryIO.read(dis), decoder)) {
      var data = reader.read(chunkRegion);
      validateChecksum(path, chunkRegion.checksum(), checksum.getValue());
      return data;
    }
  }

  @Override
  public CompletableFuture<Collection<Snapshot>> loadDataAsync(String name, Collection<ChunkRegion.Validated> chunkRegions) {
    return plugin.coordinator().executor().async().submit(() -> {
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
      plugin.logger().error(e.getMessage(), e);
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
         var writer = new SchemWriter(dos, decoder.dataVersion())) {
      writer.write(snapshot);
    }
    return checksum.getValue();
  }

  @Override
  public CompletableFuture<Collection<ChunkRegion.Validated>> saveDataAsync(String name, Iterable<Snapshot> data) {
    return plugin.coordinator().executor().async().submit(() -> {
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
      plugin.logger().error(e.getMessage(), e);
      return List.of();
    });
  }

  private @Nullable Arena loadArena(Path path) {
    Arena arena = null;
    try (var reader = new JsonReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
      arena = gson.fromJson(reader, Arena.class);
      for (var chunk : arena.chunks()) {
        var chunkPath = chunkPath(arena.name(), chunk);
        validateChecksum(chunkPath, chunk.checksum(), calculateChecksum(chunkPath));
      }
    } catch (IOException | JsonParseException e) {
      plugin.logger().error(e.getMessage(), e);
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
      cis.readAllBytes();
      while (cis.read(buffer) >= 0) {
      }
      return checksum.getValue();
    }
  }
}
