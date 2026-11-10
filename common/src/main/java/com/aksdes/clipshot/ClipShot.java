package com.aksdes.clipshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Loader-independent core. The Fabric and NeoForge entrypoints only pass in the
 * folders their loader uses and register the settings screen.
 */
public final class ClipShot {
	public static final String MOD_ID = "clipshot";
	public static final Logger LOGGER = LoggerFactory.getLogger("ClipShot");

	private static Path gameDir = Path.of(".");

	static {
		// Minecraft keeps AWT headless, which blocks the system clipboard.
		// Turning it back on lets us copy straight from Java (fast, no external tools).
		// Not on macOS: AWT and GLFW don't get along there, osascript is used instead.
		// Must run before anything asks GraphicsEnvironment.isHeadless() (the value is cached).
		if (!ClipboardCopier.IS_MAC) {
			System.setProperty("java.awt.headless", "false");
		}
	}

	private ClipShot() {
	}

	public static void init(String loader, Path configDir, Path gameDirectory) {
		gameDir = gameDirectory;
		ClipShotConfig.load(configDir);
		LOGGER.info("ClipShot loaded on {} (copy: {}, notifications: {})", loader,
				ClipShotConfig.get().copyToClipboard,
				ClipShotConfig.get().showNotifications);
	}

	public static Path gameDir() {
		return gameDir;
	}
}
