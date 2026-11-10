package com.aksdes.clipshot;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClipShot implements ClientModInitializer {
	public static final String MOD_ID = "clipshot";
	public static final Logger LOGGER = LoggerFactory.getLogger("ClipShot");

	static {
		// Minecraft keeps AWT headless, which blocks the system clipboard.
		// Turning it back on lets us copy straight from Java (fast, no external tools).
		// Not on macOS: AWT and GLFW don't get along there, osascript is used instead.
		// Must run before anything asks GraphicsEnvironment.isHeadless() (the value is cached).
		if (!ClipboardCopier.IS_MAC) {
			System.setProperty("java.awt.headless", "false");
		}
	}

	@Override
	public void onInitializeClient() {
		ClipShotConfig.load();
		LOGGER.info("ClipShot loaded (copy: {}, notifications: {})",
				ClipShotConfig.get().copyToClipboard,
				ClipShotConfig.get().showNotifications);
	}
}
