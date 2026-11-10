package com.aksdes.clipshot;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Puts a PNG file into the system clipboard as an image.
 * <ul>
 *   <li>Windows: AWT (instant), PowerShell as a fallback.</li>
 *   <li>Linux: wl-copy / xclip (keep the image after the game is closed), AWT as a fallback,
 *       so it still works with nothing installed.</li>
 *   <li>macOS: osascript (AWT is not safe next to GLFW there).</li>
 * </ul>
 */
public final class ClipboardCopier {
	private static final String OS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
	public static final boolean IS_WINDOWS = OS.contains("win");
	public static final boolean IS_MAC = OS.contains("mac");
	private static final long TIMEOUT_SECONDS = 15;

	private ClipboardCopier() {
	}

	/** @return true if the image ended up in the clipboard */
	public static boolean copy(File png) {
		if (png == null || !png.isFile()) {
			Clipshot.LOGGER.warn("Screenshot file not found: {}", png);
			return false;
		}
		try {
			if (IS_WINDOWS) {
				return copyWithAwt(png) || copyWindows(png);
			} else if (IS_MAC) {
				return copyMac(png);
			} else {
				return copyLinux(png) || copyWithAwt(png);
			}
		} catch (Exception e) {
			Clipshot.LOGGER.error("Failed to copy screenshot to clipboard", e);
			return false;
		}
	}

	// ---------------------------------------------------------------- AWT

	private static boolean copyWithAwt(File png) {
		try {
			if (GraphicsEnvironment.isHeadless()) {
				Clipshot.LOGGER.debug("AWT is headless, skipping");
				return false;
			}
			byte[] bytes = Files.readAllBytes(png.toPath());
			BufferedImage image = javax.imageio.ImageIO.read(new ByteArrayInputStream(bytes));
			if (image == null) {
				Clipshot.LOGGER.warn("Could not decode {}", png);
				return false;
			}
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new PngTransferable(bytes, image), null);
			return true;
		} catch (Throwable t) {
			Clipshot.LOGGER.debug("AWT clipboard failed", t);
			return false;
		}
	}

	/**
	 * Offers both the raw PNG (keeps exact pixels, preferred by Discord/browsers)
	 * and a decoded image (for Paint, Word and older apps).
	 */
	private record PngTransferable(byte[] png, BufferedImage image) implements Transferable {
		private static final DataFlavor PNG = createPngFlavor();

		private static DataFlavor createPngFlavor() {
			try {
				return new DataFlavor("image/png;class=java.io.InputStream");
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{PNG, DataFlavor.imageFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return PNG.equals(flavor) || DataFlavor.imageFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (PNG.equals(flavor)) {
				return new ByteArrayInputStream(png);
			}
			if (DataFlavor.imageFlavor.equals(flavor)) {
				return image;
			}
			throw new UnsupportedFlavorException(flavor);
		}
	}

	// ---------------------------------------------------------------- Windows fallback

	private static boolean copyWindows(File png) throws IOException, InterruptedException {
		// The path is passed via an environment variable, so spaces/quotes/Cyrillic in it are safe.
		String script = String.join("; ",
				"$ErrorActionPreference = 'Stop'",
				"Add-Type -AssemblyName System.Windows.Forms",
				"Add-Type -AssemblyName System.Drawing",
				"$bytes = [System.IO.File]::ReadAllBytes($env:CLIPSHOT_FILE)",
				"$png = New-Object System.IO.MemoryStream(,$bytes)",
				"$src = New-Object System.IO.MemoryStream(,$bytes)",
				"$img = [System.Drawing.Image]::FromStream($src)",
				"$data = New-Object System.Windows.Forms.DataObject",
				"$data.SetData('PNG', $false, $png)",
				"$data.SetImage($img)",
				"[System.Windows.Forms.Clipboard]::SetDataObject($data, $true)",
				"$img.Dispose()");

		ProcessBuilder pb = new ProcessBuilder("powershell.exe",
				"-NoProfile", "-NonInteractive", "-STA", "-ExecutionPolicy", "Bypass",
				"-Command", script);
		pb.environment().put("CLIPSHOT_FILE", png.getAbsolutePath());
		return run(pb);
	}

	// ---------------------------------------------------------------- macOS

	private static boolean copyMac(File png) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("osascript",
				"-e", "on run argv",
				"-e", "set the clipboard to (read (POSIX file (item 1 of argv)) as «class PNGf»)",
				"-e", "end run",
				png.getAbsolutePath());
		return run(pb);
	}

	// ---------------------------------------------------------------- Linux

	private static boolean copyLinux(File png) {
		List<ProcessBuilder> attempts = new ArrayList<>();
		String wayland = System.getenv("WAYLAND_DISPLAY");
		String x11 = System.getenv("DISPLAY");

		if (wayland != null && !wayland.isEmpty()) {
			attempts.add(new ProcessBuilder("wl-copy", "--type", "image/png").redirectInput(png));
		}
		if (x11 != null && !x11.isEmpty()) {
			attempts.add(new ProcessBuilder("xclip", "-selection", "clipboard", "-t", "image/png", "-i", png.getAbsolutePath()));
		}
		// Flatpak launchers (e.g. Prism on Bazzite/SteamOS) — only works if the sandbox may talk to the host.
		if (new File("/.flatpak-info").exists()) {
			attempts.add(new ProcessBuilder("flatpak-spawn", "--host", "wl-copy", "--type", "image/png").redirectInput(png));
			attempts.add(new ProcessBuilder("flatpak-spawn", "--host", "xclip", "-selection", "clipboard", "-t", "image/png", "-i", png.getAbsolutePath()));
		}

		for (ProcessBuilder pb : attempts) {
			try {
				if (run(pb)) {
					return true;
				}
			} catch (IOException e) {
				Clipshot.LOGGER.debug("{} is not available", pb.command().get(0));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}
		Clipshot.LOGGER.debug("No wl-copy/xclip, trying AWT");
		return false;
	}

	// ---------------------------------------------------------------- helpers

	private static boolean run(ProcessBuilder pb) throws IOException, InterruptedException {
		// wl-copy / xclip fork into the background to serve the clipboard;
		// discarding the output keeps us from waiting on their pipes forever.
		pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
		pb.redirectError(ProcessBuilder.Redirect.DISCARD);
		Process process = pb.start();
		if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			process.destroyForcibly();
			Clipshot.LOGGER.warn("{} timed out", pb.command().get(0));
			return false;
		}
		int code = process.exitValue();
		if (code != 0) {
			Clipshot.LOGGER.warn("{} exited with code {}", pb.command().get(0), code);
		}
		return code == 0;
	}
}
