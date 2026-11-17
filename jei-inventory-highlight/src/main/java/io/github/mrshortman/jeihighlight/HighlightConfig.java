package io.github.mrshortman.jeihighlight;

import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/** The mod's settings, kept in {@code config/jei_inventory_highlight.properties}. */
public final class HighlightConfig {
	private static final String KEY_ENABLED = "enabled";
	private static final String KEY_HIGHLIGHT_COLOR = "highlight_color";
	private static final String KEY_DIM_OPACITY = "dim_opacity";

	/** RGB of the frame around matching slots and around the JEI search bar. */
	public static final int DEFAULT_HIGHLIGHT_COLOR = 0xFFD83D;
	/** Opacity of the layer that greys out non-matching slots, in percent. */
	public static final int DEFAULT_DIM_OPACITY = 75;

	private static boolean enabled;
	private static int highlightColor = DEFAULT_HIGHLIGHT_COLOR;
	private static int dimOpacity = DEFAULT_DIM_OPACITY;

	private HighlightConfig() {
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void toggle() {
		enabled = !enabled;
		save();
	}

	/** @return the highlight colour as RGB, without alpha */
	public static int highlightColor() {
		return highlightColor;
	}

	/** @return the opacity of the dimming layer in percent, 0 to 100 */
	public static int dimOpacity() {
		return dimOpacity;
	}

	/** Applies and saves new settings, e.g. from the settings screen. */
	public static void set(int newHighlightColor, int newDimOpacity) {
		highlightColor = newHighlightColor & 0xFFFFFF;
		dimOpacity = clampOpacity(newDimOpacity);
		save();
	}

	static int clampOpacity(int percent) {
		return Math.clamp(percent, 0, 100);
	}

	/** @return {@code #RRGGBB} */
	public static String formatColor(int rgb) {
		return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
	}

	/** Accepts {@code #RRGGBB} and {@code RRGGBB}. @return the RGB value, or null if the text is not a colour */
	public static @Nullable Integer parseColor(String text) {
		String hex = text.strip();
		if (hex.startsWith("#")) {
			hex = hex.substring(1);
		}
		if (hex.length() != 6) {
			return null;
		}
		try {
			return Integer.parseInt(hex, 16);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	static void load() {
		Path file = file();
		if (!Files.isRegularFile(file)) {
			return;
		}
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(file)) {
			properties.load(reader);
		} catch (IOException | IllegalArgumentException e) {
			JeiInventoryHighlight.LOGGER.warn("Could not read {}", file, e);
			return;
		}
		enabled = Boolean.parseBoolean(properties.getProperty(KEY_ENABLED));

		String color = properties.getProperty(KEY_HIGHLIGHT_COLOR);
		if (color != null) {
			Integer parsed = parseColor(color);
			if (parsed != null) {
				highlightColor = parsed;
			} else {
				JeiInventoryHighlight.LOGGER.warn("Ignoring {}={} in {}, expected #RRGGBB", KEY_HIGHLIGHT_COLOR, color, file);
			}
		}

		String opacity = properties.getProperty(KEY_DIM_OPACITY);
		if (opacity != null) {
			try {
				dimOpacity = clampOpacity(Integer.parseInt(opacity.strip()));
			} catch (NumberFormatException e) {
				JeiInventoryHighlight.LOGGER.warn("Ignoring {}={} in {}, expected 0 to 100", KEY_DIM_OPACITY, opacity, file);
			}
		}
	}

	private static void save() {
		Path file = file();
		Properties properties = new Properties();
		properties.setProperty(KEY_ENABLED, Boolean.toString(enabled));
		// Without the '#', which Properties would escape.
		properties.setProperty(KEY_HIGHLIGHT_COLOR, String.format(Locale.ROOT, "%06X", highlightColor));
		properties.setProperty(KEY_DIM_OPACITY, Integer.toString(dimOpacity));
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
