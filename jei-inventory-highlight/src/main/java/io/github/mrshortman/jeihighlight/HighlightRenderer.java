package io.github.mrshortman.jeihighlight;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public final class HighlightRenderer {
	private static final int SLOT_SIZE = 16;
	/** ARGB: darkens every slot that does not match the search. */
	private static final int DIM_COLOR = 0xC0101010;
	/** ARGB: frame around matching slots and around the JEI search bar while the mode is on. */
	private static final int ACCENT_COLOR = 0xFFFFD83D;

	private HighlightRenderer() {
	}

	/**
	 * Called right after the container screen has drawn its slots, with the pose already moved to the
	 * top-left corner of the container, i.e. slot coordinates can be used as they are.
	 */
	public static void extractSlotOverlay(GuiGraphicsExtractor graphics, List<Slot> slots) {
		if (!HighlightConfig.isEnabled()) {
			return;
		}
		Predicate<ItemStack> matcher = JeiSearch.currentMatcher();
		if (matcher == null) {
			return;
		}
		for (Slot slot : slots) {
			if (!slot.isActive()) {
				continue;
			}
			if (matcher.test(slot.getItem())) {
				graphics.outline(slot.x - 1, slot.y - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, ACCENT_COLOR);
			} else {
				graphics.fill(slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, DIM_COLOR);
			}
		}
	}

	/** Frames the JEI search bar so it is visible at a glance that the highlight mode is switched on. */
	public static void extractSearchFieldIndicator(GuiGraphicsExtractor graphics) {
		if (!HighlightConfig.isEnabled()) {
			return;
		}
		EditBox field = JeiSearch.visibleSearchField();
		if (field == null) {
			return;
		}
		// JEI insets the text inside the bar's background: 4px on the left, 8px on the right, vertically centred.
		int height = field.getHeight();
		int x = field.getX() - 4;
		int y = field.getY() - (height - 8) / 2;
		int width = field.getWidth() + 12;
		graphics.outline(x, y, width, height, ACCENT_COLOR);
	}
}
