package io.github.mrshortman.jeihighlight;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JeiInventoryHighlight implements ClientModInitializer {
	public static final String MOD_ID = "jei_inventory_highlight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** JEI consumes clicks on its search bar, so our listener has to run before JEI's. */
	private static final Identifier BEFORE_JEI = Identifier.fromNamespaceAndPath(MOD_ID, "before_jei");
	private static final long DOUBLE_CLICK_MILLIS = 300;

	/** Opens the settings screen; unbound by default, Mod Menu's "Configure" button opens it too. */
	private static final KeyMapping OPEN_SETTINGS = new KeyMapping(
		"key." + MOD_ID + ".settings", InputConstants.UNKNOWN.getValue(), KeyMapping.Category.INVENTORY);

	private static boolean awaitingSecondClick;
	private static long firstClickMillis;

	@Override
	public void onInitializeClient() {
		HighlightConfig.load();

		KeyMappingHelper.registerKeyMapping(OPEN_SETTINGS);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_SETTINGS.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new HighlightSettingsScreen(null));
				}
			}
		});

		// Screen events are reset on every init, so they are registered again each time (JEI does the same).
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			Event<ScreenMouseEvents.AllowMouseClick> mouseClick = ScreenMouseEvents.allowMouseClick(screen);
			mouseClick.addPhaseOrdering(BEFORE_JEI, Event.DEFAULT_PHASE);
			mouseClick.register(BEFORE_JEI, JeiInventoryHighlight::onMouseClick);
		});
	}

	private static boolean onMouseClick(Screen screen, MouseButtonEvent event) {
		if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || !JeiSearch.isOverSearchField(event.x(), event.y())) {
			awaitingSecondClick = false;
			return true;
		}

		long now = Util.getMillis();
		if (awaitingSecondClick && now - firstClickMillis <= DOUBLE_CLICK_MILLIS) {
			awaitingSecondClick = false;
			HighlightConfig.toggle();
		} else {
			awaitingSecondClick = true;
			firstClickMillis = now;
		}
		// Never swallow the click: JEI still focuses its search bar as usual.
		return true;
	}
}
