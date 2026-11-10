package com.aksdes.clipshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Settings stored in config/clipshot.json.
 */
public final class ClipShotConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "clipshot.json";
	/** Set by {@link #load(Path)}; the loader decides where the config folder is. */
	private static Path file = Path.of("config", FILE_NAME);

	private static volatile ClipShotConfig instance = new ClipShotConfig();

	/** Copy F2 screenshots to the clipboard. */
	public boolean copyToClipboard = true;
	/** Show a chat message after the screenshot was copied (or failed to copy). */
	public boolean showNotifications = true;

	public static ClipShotConfig get() {
		return instance;
	}

	public static void load(Path configDir) {
		file = configDir.resolve(FILE_NAME);
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				ClipShotConfig loaded = GSON.fromJson(reader, ClipShotConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (Exception e) {
				ClipShot.LOGGER.warn("Could not read {}, using defaults", file, e);
			}
		}
		save(); // writes defaults / adds new keys
	}

	public static void save() {
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			ClipShot.LOGGER.warn("Could not save {}", file, e);
		}
	}
}
