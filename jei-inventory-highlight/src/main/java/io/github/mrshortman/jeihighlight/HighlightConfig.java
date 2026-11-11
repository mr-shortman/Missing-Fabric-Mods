package io.github.mrshortman.jeihighlight;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Remembers across restarts whether the highlight mode is switched on. */
public final class HighlightConfig {
	private static final String KEY_ENABLED = "enabled";

	private static boolean enabled;

	private HighlightConfig() {
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void toggle() {
		enabled = !enabled;
		save();
	}

	static void load() {
		Path file = file();
		if (!Files.isRegularFile(file)) {
			return;
		}
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(file)) {
			properties.load(reader);
			enabled = Boolean.parseBoolean(properties.getProperty(KEY_ENABLED));
		} catch (IOException | IllegalArgumentException e) {
			JeiInventoryHighlight.LOGGER.warn("Could not read {}", file, e);
		}
	}

	private static void save() {
		Path file = file();
		Properties properties = new Properties();
		properties.setProperty(KEY_ENABLED, Boolean.toString(enabled));
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				properties.store(writer, "JEI Inventory Highlight");
			}
		} catch (IOException e) {
			JeiInventoryHighlight.LOGGER.warn("Could not write {}", file, e);
		}
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve(JeiInventoryHighlight.MOD_ID + ".properties");
	}
}
