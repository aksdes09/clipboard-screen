package com.aksdes.clipshot;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class ScreenshotHandler {
	private static final String SUCCESS_KEY = "screenshot.success";
	private static final String FAILURE_KEY = "screenshot.failure";
	/** How recent the newest file must be when we fall back to "newest .png in the folder". */
	private static final long FALLBACK_MAX_AGE_MS = 30_000;

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "Clipshot");
		thread.setDaemon(true);
		return thread;
	});

	private ScreenshotHandler() {
	}

	/**
	 * Wraps the callback that vanilla uses to print "Saved screenshot as ..." in chat.
	 * It is called after the PNG has been written to disk, so that is when we copy it.
	 * Our own notification goes through the same callback, i.e. it lands in chat exactly
	 * like the vanilla message.
	 */
	public static Consumer<Component> wrap(Consumer<Component> vanilla) {
		return message -> {
			vanilla.accept(message);

			ClipshotConfig config = ClipshotConfig.get();
			if (!config.copyToClipboard || isFailure(message)) {
				return;
			}

			EXECUTOR.execute(() -> {
				boolean copied = ClipboardCopier.copy(findScreenshot(message));
				if (ClipshotConfig.get().showNotifications) {
					vanilla.accept(copied
							? Component.translatable("clipshot.message.copied").withStyle(ChatFormatting.GREEN)
							: Component.translatable("clipshot.message.failed").withStyle(ChatFormatting.RED));
				}
			});
		};
	}

	private static boolean isFailure(Component message) {
		return message.getContents() instanceof TranslatableContents tc && FAILURE_KEY.equals(tc.getKey());
	}

	private static File findScreenshot(Component message) {
		File dir = FabricLoader.getInstance().getGameDir().resolve("screenshots").toFile();

		// "screenshot.success" has the file name as its first argument.
		if (message.getContents() instanceof TranslatableContents tc
				&& SUCCESS_KEY.equals(tc.getKey())
				&& tc.getArgs().length > 0) {
			Object arg = tc.getArgs()[0];
			String name = arg instanceof Component c ? c.getString() : String.valueOf(arg);
			File file = new File(dir, name);
			if (file.isFile()) {
				return file;
			}
		}

		// Fallback: the newest PNG that was just written.
		File[] pngs = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
		if (pngs == null || pngs.length == 0) {
			return null;
		}
		File newest = Arrays.stream(pngs).max(Comparator.comparingLong(File::lastModified)).orElse(null);
		if (newest != null && System.currentTimeMillis() - newest.lastModified() <= FALLBACK_MAX_AGE_MS) {
			return newest;
		}
		return null;
	}
}
