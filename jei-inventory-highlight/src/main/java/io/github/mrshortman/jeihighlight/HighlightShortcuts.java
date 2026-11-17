package io.github.mrshortman.jeihighlight;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * Keyboard ways to switch the highlight mode on and off, besides double-clicking the JEI search bar:
 * <ul>
 *     <li>a key binding (H by default) that works in every container screen and in-game,</li>
 *     <li>pressing the inventory key twice quickly: the first press opens the inventory, the second one toggles
 *     the mode instead of closing it again.</li>
 * </ul>
 * Switching the mode on puts the cursor into the JEI search bar if it is empty, so one can start typing right away.
 */
final class HighlightShortcuts {
	static final KeyMapping TOGGLE_HIGHLIGHT = new KeyMapping(
		"key." + JeiInventoryHighlight.MOD_ID + ".toggle", InputConstants.KEY_H, KeyMapping.Category.INVENTORY);

	/** How long after the inventory opened a second press of the inventory key counts as a double tap. */
	private static final long DOUBLE_TAP_MILLIS = 350;

	private static WeakReference<Screen> inventoryScreen = new WeakReference<>(null);
	private static long inventoryOpenedMillis;
	/** Set after a toggle until its key is released, so that holding the key down does not flip the mode on every repeat. */
	private static boolean toggleKeyHeld;
	/**
	 * Focusing the search bar right away would let the character of the key that was just pressed (the "e" of the
	 * double tap, the "h" of the toggle key) land in it, so it waits until that input has been handled.
	 */
	private static boolean focusSearchBeforeNextFrame;

	private HighlightShortcuts() {
	}

	/** Called for every screen before its init, with the events of that screen. */
	static void register(Screen screen, Identifier phase) {
		if (!(screen instanceof AbstractContainerScreen<?>)) {
			return;
		}
		if ((screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)
			&& inventoryScreen.get() != screen) {
			// Init also runs on every resize; only the first one is the moment the inventory was opened.
			inventoryScreen = new WeakReference<>(screen);
			inventoryOpenedMillis = Util.getMillis();
		}
		toggleKeyHeld = false;
		focusSearchBeforeNextFrame = false;

		Event<ScreenKeyboardEvents.AllowKeyPress> keyPress = ScreenKeyboardEvents.allowKeyPress(screen);
		keyPress.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
		keyPress.register(phase, HighlightShortcuts::onKeyPress);
		ScreenEvents.beforeExtract(screen).register((s, graphics, mouseX, mouseY, partialTick) -> {
			if (focusSearchBeforeNextFrame) {
				focusSearchBeforeNextFrame = false;
				JeiSearch.focusSearchFieldIfEmpty();
			}
		});
		ScreenKeyboardEvents.afterKeyRelease(screen).register((s, event) -> {
			if (TOGGLE_HIGHLIGHT.matches(event)) {
				toggleKeyHeld = false;
		focusSearchBeforeNextFrame = false;
			}
		});
	}

	/** @return false to swallow the key press */
	private static boolean onKeyPress(Screen screen, KeyEvent event) {
		if (isTyping(screen)) {
			return true;
		}
		if (TOGGLE_HIGHLIGHT.matches(event)) {
			if (!toggleKeyHeld) {
				toggleKeyHeld = true;
				toggleInScreen();
			}
			return false;
		}
		if (HighlightConfig.doubleTapInventory()
			&& screen == inventoryScreen.get()
			&& Util.getMillis() - inventoryOpenedMillis <= DOUBLE_TAP_MILLIS
			&& Minecraft.getInstance().options.keyInventory.matches(event)) {
			// Only once: a third press closes the inventory as usual.
			inventoryOpenedMillis = 0;
			toggleInScreen();
			return false;
		}
		return true;
	}

	/** The key binding pressed while no screen is open: there is nothing to see, so say what happened. */
	static void toggleInGame(Minecraft client) {
		HighlightConfig.toggle();
		client.gui.setOverlayMessage(Component.translatable(
			"message." + JeiInventoryHighlight.MOD_ID + (HighlightConfig.isEnabled() ? ".enabled" : ".disabled")), false);
	}

	private static void toggleInScreen() {
		HighlightConfig.toggle();
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		focusSearchBeforeNextFrame = HighlightConfig.isEnabled();
	}

	/** Whether a key press would type into a text field: the JEI search bar, the creative search, an anvil, ... */
	private static boolean isTyping(Screen screen) {
		EditBox jeiField = JeiSearch.visibleSearchField();
		if (jeiField != null && jeiField.isFocused()) {
			return true;
		}
		for (GuiEventListener child : screen.children()) {
			if (child instanceof EditBox box && box.isVisible() && box.isFocused()) {
				return true;
			}
		}
		// Text fields nested in a component, e.g. the search of the recipe book.
		@Nullable GuiEventListener focused = screen.getFocused();
		while (focused instanceof ContainerEventHandler container && !(focused instanceof EditBox)) {
			focused = container.getFocused();
		}
		return focused instanceof EditBox box && box.isVisible() && box.isFocused();
	}
}
