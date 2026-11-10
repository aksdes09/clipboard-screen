package com.aksdes.clipboardscreen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Settings stored in config/clipboardscreen.json.
 */
public final class ClipboardScreenConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("clipboardscreen.json");

	private static volatile ClipboardScreenConfig instance = new ClipboardScreenConfig();

	/** Copy F2 screenshots to the clipboard. */
	public boolean copyToClipboard = true;
	/** Show a chat message after the screenshot was copied (or failed to copy). */
	public boolean showNotifications = true;

	public static ClipboardScreenConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(FILE)) {
			try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
				ClipboardScreenConfig loaded = GSON.fromJson(reader, ClipboardScreenConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (Exception e) {
				ClipboardScreen.LOGGER.warn("Could not read {}, using defaults", FILE, e);
			}
		}
		save(); // writes defaults / adds new keys
	}

	public static void save() {
		try {
			Files.createDirectories(FILE.getParent());
			try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			ClipboardScreen.LOGGER.warn("Could not save {}", FILE, e);
		}
	}
}
