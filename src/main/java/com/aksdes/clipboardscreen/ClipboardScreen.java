package com.aksdes.clipboardscreen;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClipboardScreen implements ClientModInitializer {
	public static final String MOD_ID = "clipboardscreen";
	public static final Logger LOGGER = LoggerFactory.getLogger("Clipboard Screen");

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
		ClipboardScreenConfig.load();
		LOGGER.info("Clipboard Screen loaded (copy: {}, notifications: {})",
				ClipboardScreenConfig.get().copyToClipboard,
				ClipboardScreenConfig.get().showNotifications);
	}
}
