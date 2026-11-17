package io.github.mrshortman.jeihighlight;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Registered through the {@code modmenu} entrypoint, only loaded when Mod Menu is installed. */
public final class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return HighlightSettingsScreen::new;
	}
}
