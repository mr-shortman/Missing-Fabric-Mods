package io.github.mrshortman.jeihighlight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class HighlightRenderer {
	private static final int SLOT_SIZE = 16;
	/** ARGB: darkens every slot that does not match the search. */
	private static final int DIM_COLOR = 0xC0101010;
	/** ARGB: frame around matching slots and around the JEI search bar while the mode is on. */
	private static final int ACCENT_COLOR = 0xFFFFD83D;
	/** ARGB: frame and match count for a shulker box, bundle etc. that has matching items inside. */
	private static final int INSIDE_COLOR = 0xFF55FFFF;
	private static final int MAX_SHOWN_COUNT = 99;

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
		JeiSearch.Matcher matcher = JeiSearch.currentMatcher();
		if (matcher == null) {
			return;
		}
		Font font = Minecraft.getInstance().font;
		for (Slot slot : slots) {
			if (!slot.isActive()) {
				continue;
			}
			ItemStack stack = slot.getItem();
			int inside = stack.isEmpty() ? 0 : matcher.matchesInside(stack);
			if (matcher.matches(stack)) {
				graphics.outline(slot.x - 1, slot.y - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, ACCENT_COLOR);
			} else if (inside > 0) {
				graphics.outline(slot.x - 1, slot.y - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, INSIDE_COLOR);
			} else {
				graphics.fill(slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, DIM_COLOR);
			}
			if (inside > 0) {
				// Top-left corner, so it does not collide with the stack size in the bottom-right one.
				String label = inside > MAX_SHOWN_COUNT ? MAX_SHOWN_COUNT + "+" : Integer.toString(inside);
				graphics.text(font, label, slot.x, slot.y, INSIDE_COLOR, true);
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
