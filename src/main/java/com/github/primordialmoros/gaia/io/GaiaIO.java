/*
 *   Copyright 2020 Moros <https://github.com/PrimordialMoros>
 *
 * 	  This file is part of Gaia.
 *
 *    Gaia is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Gaia is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Gaia.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.primordialmoros.gaia.io;

import com.github.primordialmoros.gaia.Arena;
import com.github.primordialmoros.gaia.ArenaManager;
import com.github.primordialmoros.gaia.Gaia;
import com.github.primordialmoros.gaia.methods.CoreMethods;
import com.github.primordialmoros.gaia.util.GaiaChunk;
import com.github.primordialmoros.gaia.util.GaiaData;
import com.github.primordialmoros.gaia.util.GaiaRegion;
import com.github.primordialmoros.gaia.util.GaiaVector;
import com.github.primordialmoros.gaia.util.Util;
import com.github.primordialmoros.gaia.util.metadata.ArenaMetadata;
import com.github.primordialmoros.gaia.util.metadata.ChunkMetadata;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sk89q.worldedit.util.io.Closer;
import org.bukkit.World;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class GaiaIO {

	public static final String ALGORITHM = "MD5";

	public static final String ARENA_SUFFIX = ".json";
	public static final String DATA_SUFFIX = ".gaia";

	private static GaiaIO IO;

	private final Path arenaDir;
	private final boolean debug;
	private final Gson gson;

	private GaiaIO(Path arenaDir, boolean debug) {
		this.arenaDir = arenaDir;
		this.debug = debug;
		gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(GaiaVector.class, new GaiaAdapter()).create();
	}

	public static boolean createInstance(String parentDirectory, boolean debug) {
		if (IO != null) return false;
		try {
			Path arenaDir = Paths.get(parentDirectory, "Arenas");
			Files.createDirectories(arenaDir);
			IO = new GaiaIO(arenaDir, debug);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return Files.exists(IO.arenaDir);
	}

	public static GaiaIO getInstance() {
		return IO;
	}

	public boolean arenaFileExists(String name) {
		Path file = Paths.get(arenaDir.toString(), name + ARENA_SUFFIX);
		return Files.exists(file);
	}

	public boolean createArenaFiles(String name) {
		try {
			Path file = Paths.get(arenaDir.toString(), name + ARENA_SUFFIX);
			Path directory = Paths.get(arenaDir.toString(), name);
			Files.createDirectories(directory);
			Files.createFile(file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean deleteArena(String name) {
		Path file = Paths.get(arenaDir.toString(), name + ARENA_SUFFIX);
		Path directory = Paths.get(arenaDir.toString(), name);
		try {
			Files.walk(directory, 1).filter(IO::isData).map(Path::toFile).forEach(File::delete);
			Files.deleteIfExists(directory);
			Files.deleteIfExists(file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean isJson(Path path) {
		return path.getFileName().toString().endsWith(ARENA_SUFFIX);
	}

	private boolean isData(Path path) {
		return path.getFileName().toString().endsWith(DATA_SUFFIX);
	}

	public void loadAllArenas() {
		try {
			Files.walk(arenaDir, 1).filter(IO::isJson).forEach(IO::loadArena);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadArena(Path path) {
		final long time = System.currentTimeMillis();
		try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8))) {
			ArenaMetadata meta = gson.fromJson(reader, ArenaMetadata.class);
			if (meta == null || !meta.isValidMetadata()) return;
			int amount = meta.amount;
			World w = CoreMethods.getWorld(UUID.fromString(meta.world));
			Arena arena = new Arena(meta.name, w, new GaiaRegion(meta.min, meta.max));
			meta.chunks.stream().filter(ChunkMetadata::isValidMetadata).forEach(m -> {
				Path chunkPath = Paths.get(arenaDir.toString(), meta.name, m.id + DATA_SUFFIX);
				if (isValidFile(chunkPath, m.hash)) {
					UUID id = UUID.fromString(m.id);
					GaiaChunk chunk = new GaiaChunk(id, arena, new GaiaRegion(m.min, m.max));
					arena.addSubRegion(chunk);
				}
			});
			if (debug && arena.getSubRegions().size() != amount) {
				Gaia.getLog().warning("Incomplete loading for arena: " + arena.getName());
			}
			if (arena.finalizeArena()) {
				Gaia.getLog().info("Loaded arena: " + arena.getName() + " (" + (System.currentTimeMillis() - time) + "ms)");
				ArenaManager.addArena(arena);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean saveArena(ArenaMetadata meta) {
		Path path = Paths.get(arenaDir.toString(), meta.name + ARENA_SUFFIX);
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
			gson.toJson(meta, writer);
			Gaia.getLog().info(meta.name + " has been stored successfully.");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public GaiaData loadData(GaiaChunk chunk) {
		Path path = Paths.get(arenaDir.toString(), chunk.getParent().getName(), chunk.getId() + DATA_SUFFIX);
		try (Closer closer = Closer.create()) {
			FileInputStream fis = closer.register(new FileInputStream(path.toFile()));
			BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
			GaiaReader reader = closer.register(GaiaReader.getReader(bis));
			return reader.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String saveData(GaiaChunk chunk, GaiaData data) {
		Path path = Paths.get(arenaDir.toString(), chunk.getParent().getName(), chunk.getId() + DATA_SUFFIX);
		DigestOutputStream hos;
		try (Closer closer = Closer.create()) {
			FileOutputStream fos = closer.register(new FileOutputStream(path.toFile()));
			hos = closer.register(new DigestOutputStream(fos, MessageDigest.getInstance(ALGORITHM)));
			BufferedOutputStream bos = closer.register(new BufferedOutputStream(hos));
			GaiaWriter writer = closer.register(GaiaWriter.getWriter(bos));
			writer.write(data);
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}
		byte[] hashBytes = hos.getMessageDigest().digest();
		String checksum = Util.toHex(hashBytes);
		chunk.setMetadata(new ChunkMetadata(chunk, checksum));
		return checksum;
	}


	private boolean isValidFile(Path path, String checksum) {
		if (!path.getFileName().toString().endsWith(DATA_SUFFIX)) return false;
		final String actualChecksum = getFileChecksum(path);
		final boolean match = checksum.equals(actualChecksum);
		if (debug && !match) {
			Gaia.getLog().warning("Checksums don't match for file: " + path.toString());
			Gaia.getLog().info("Expected: " + checksum);
			Gaia.getLog().info("Found: " + actualChecksum);
			Gaia.getLog().warning("Your data might be corrupted. Arena won't load for safety reasons.");
		}
		return match;
	}

	private String getFileChecksum(Path filePath) {
		byte[] buffer = new byte[65536];
		try (FileInputStream stream = new FileInputStream(filePath.toFile())) {
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);
			int bytesRead;
			while ((bytesRead = stream.read(buffer)) > 0) {
				md.update(buffer, 0, bytesRead);
			}
			return Util.toHex(md.digest());
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}
