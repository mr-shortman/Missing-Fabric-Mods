package io.github.mrshortman.jeihighlight;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets the player pick the highlight colour and how strongly non-matching slots are greyed out, with a small
 * preview of both. Esc and "Done" save, "Cancel" throws the changes away.
 */
public final class HighlightSettingsScreen extends Screen {
	private static final String KEY = "screen." + JeiInventoryHighlight.MOD_ID + ".settings.";

	private static final int COLUMN_WIDTH = 200;
	private static final int ROW_HEIGHT = 20;
	private static final int ROW_SPACING = 4;
	private static final int LABEL_HEIGHT = 12;
	private static final int MIN_SECTION_SPACING = 4;
	private static final int MAX_SECTION_SPACING = 12;
	private static final int BUTTON_WIDTH = 100;
	private static final int BUTTON_SPACING = 5;

	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int LABEL_COLOR = 0xFFA0A0A0;
	private static final int INVALID_TEXT_COLOR = 0xFFFF5555;

	// Vanilla inventory look for the preview.
	private static final int PANEL_COLOR = 0xFFC6C6C6;
	private static final int SLOT_COLOR = 0xFF8B8B8B;
	private static final int SLOT_SHADOW = 0xFF373737;
	private static final int SLOT_LIGHT = 0xFFFFFFFF;
	private static final int SLOT_PITCH = 18;
	private static final int PANEL_PADDING = 7;

	/**
	 * What the preview shows; the diamonds play the items that match the search. Each slot also has a plain
	 * colour to show instead of the item, for when the screen is opened from the title screen: there are no item
	 * components before a world is loaded, so no ItemStack can be created or drawn.
	 */
	private record PreviewSlot(Item item, int count, int placeholderColor, boolean matches) {
	}

	private static final List<PreviewSlot> PREVIEW = List.of(
		new PreviewSlot(Items.DIAMOND, 3, 0xFF4AEDD9, true),
		new PreviewSlot(Items.COBBLESTONE, 64, 0xFF7A7A7A, false),
		new PreviewSlot(Items.OAK_LOG, 16, 0xFF6B5130, false),
		new PreviewSlot(Items.DIAMOND, 1, 0xFF4AEDD9, true),
		new PreviewSlot(Items.APPLE, 5, 0xFFD52A2A, false),
		new PreviewSlot(Items.IRON_INGOT, 32, 0xFFD8D8D8, false),
		new PreviewSlot(Items.TORCH, 48, 0xFFFFC94A, false)
	);

	private final @Nullable Screen parent;

	private int highlightColor;
	private int dimOpacity;

	private @Nullable EditBox hexField;
	private final List<ChannelSlider> channelSliders = new ArrayList<>();
	private @Nullable OpacitySlider opacitySlider;
	/** Set while widgets are being brought in line with each other, so they do not keep updating one another. */
	private boolean syncing;

	// Where init() put things, for drawing the labels, the swatch and the preview.
	private int left;
	private int colorLabelY;
	private int dimLabelY;
	private int previewLabelY;
	private int previewY;
	/** Whether init() left out the headings of the dimming and preview sections for lack of room. */
	private boolean compact;

	public HighlightSettingsScreen(@Nullable Screen parent) {
		super(Component.translatable(KEY + "title"));
		this.parent = parent;
		this.highlightColor = HighlightConfig.highlightColor();
		this.dimOpacity = HighlightConfig.dimOpacity();
	}

	@Override
	protected void init() {
		channelSliders.clear();
		left = width / 2 - COLUMN_WIDTH / 2;

		int buttonsY = height - ROW_HEIGHT - 8;
		int top = 32;
		int available = buttonsY - 4 - top;
		int previewHeight = SLOT_PITCH + 2 * PANEL_PADDING;
		int controlsHeight = LABEL_HEIGHT + 4 * (ROW_HEIGHT + ROW_SPACING) + ROW_HEIGHT + previewHeight;
		// On a short screen the two smaller headings go, the opacity slider then says what it is about.
		int fullHeight = controlsHeight + 2 * LABEL_HEIGHT + 2 * MAX_SECTION_SPACING;
		compact = available < fullHeight;
		int contentHeight = compact ? controlsHeight : controlsHeight + 2 * LABEL_HEIGHT;
		int sectionSpacing = Math.clamp((available - contentHeight) / 2, MIN_SECTION_SPACING, MAX_SECTION_SPACING);
		contentHeight += 2 * sectionSpacing;
		// Centre the content between the title and the buttons, but never let it slide under the title.
		int y = top + Math.max(0, (available - contentHeight) / 2);

		colorLabelY = y;
		y += LABEL_HEIGHT;
		EditBox hex = new EditBox(font, left, y, COLUMN_WIDTH - ROW_HEIGHT - ROW_SPACING, ROW_HEIGHT, Component.translatable(KEY + "highlight_color"));
		hex.setMaxLength(7);
		hex.setHint(Component.literal("#RRGGBB"));
		hex.setValue(HighlightConfig.formatColor(highlightColor));
		hex.setResponder(this::onHexChanged);
		hexField = addRenderableWidget(hex);
		y += ROW_HEIGHT + ROW_SPACING;

		String[] channels = {"red", "green", "blue"};
		for (int i = 0; i < channels.length; i++) {
			int shift = 16 - 8 * i;
			channelSliders.add(addRenderableWidget(new ChannelSlider(left, y, Component.translatable(KEY + channels[i]), shift)));
			y += ROW_HEIGHT + ROW_SPACING;
		}

		y += sectionSpacing - ROW_SPACING;
		dimLabelY = y;
		if (!compact) {
			y += LABEL_HEIGHT;
		}
		opacitySlider = addRenderableWidget(new OpacitySlider(left, y));
		y += ROW_HEIGHT;

		y += sectionSpacing;
		previewLabelY = y;
		previewY = compact ? y : y + LABEL_HEIGHT;

		int buttonsLeft = width / 2 - (3 * BUTTON_WIDTH + 2 * BUTTON_SPACING) / 2;
		addRenderableWidget(Button.builder(Component.translatable(KEY + "defaults"), button -> resetToDefaults())
			.bounds(buttonsLeft, buttonsY, BUTTON_WIDTH, ROW_HEIGHT)
			.build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> close(false))
			.bounds(buttonsLeft + BUTTON_WIDTH + BUTTON_SPACING, buttonsY, BUTTON_WIDTH, ROW_HEIGHT)
			.build());
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> close(true))
			.bounds(buttonsLeft + 2 * (BUTTON_WIDTH + BUTTON_SPACING), buttonsY, BUTTON_WIDTH, ROW_HEIGHT)
			.build());
	}

	private void onHexChanged(String text) {
		if (syncing || hexField == null) {
			return;
		}
		Integer parsed = HighlightConfig.parseColor(text);
		hexField.setTextColor(parsed != null ? EditBox.DEFAULT_TEXT_COLOR : INVALID_TEXT_COLOR);
		if (parsed != null) {
			highlightColor = parsed;
			syncWidgets(false);
		}
	}

	private void resetToDefaults() {
		highlightColor = HighlightConfig.DEFAULT_HIGHLIGHT_COLOR;
		dimOpacity = HighlightConfig.DEFAULT_DIM_OPACITY;
		syncWidgets(true);
	}

	/** Brings every widget in line with {@link #highlightColor} and {@link #dimOpacity}. */
	private void syncWidgets(boolean includingHexField) {
		syncing = true;
		try {
			if (includingHexField && hexField != null) {
				hexField.setValue(HighlightConfig.formatColor(highlightColor));
				hexField.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
			}
			for (ChannelSlider slider : channelSliders) {
				slider.sync();
			}
			if (opacitySlider != null) {
				opacitySlider.sync();
			}
		} finally {
			syncing = false;
		}
	}

	private void close(boolean save) {
		if (save) {
			HighlightConfig.set(highlightColor, dimOpacity);
		}
		minecraft.setScreen(parent);
	}

	@Override
	public void onClose() {
		close(true);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(font, title, width / 2, 15, TEXT_COLOR);

		graphics.text(font, Component.translatable(KEY + "highlight_color"), left, colorLabelY + 2, LABEL_COLOR);
		if (!compact) {
			graphics.text(font, Component.translatable(KEY + "dimmed_slots"), left, dimLabelY + 2, LABEL_COLOR);
			graphics.text(font, Component.translatable(KEY + "preview"), left, previewLabelY + 2, LABEL_COLOR);
		}

		if (hexField != null) {
			int x = left + COLUMN_WIDTH - ROW_HEIGHT;
			int y = hexField.getY();
			graphics.fill(x, y, x + ROW_HEIGHT, y + ROW_HEIGHT, 0xFF000000);
			graphics.fill(x + 1, y + 1, x + ROW_HEIGHT - 1, y + ROW_HEIGHT - 1, 0xFFA0A0A0);
			graphics.fill(x + 2, y + 2, x + ROW_HEIGHT - 2, y + ROW_HEIGHT - 2, HighlightRenderer.accentColor(highlightColor));
		}

		extractPreview(graphics);
	}

	/** A row of inventory slots as they look with the current settings while searching for diamonds. */
	private void extractPreview(GuiGraphicsExtractor graphics) {
		int count = PREVIEW.size();
		int panelWidth = count * SLOT_PITCH + 2 * PANEL_PADDING;
		int panelLeft = width / 2 - panelWidth / 2;
		graphics.fill(panelLeft, previewY, panelLeft + panelWidth, previewY + SLOT_PITCH + 2 * PANEL_PADDING, PANEL_COLOR);

		int accent = HighlightRenderer.accentColor(highlightColor);
		int dim = HighlightRenderer.dimColor(dimOpacity);
		int y = previewY + PANEL_PADDING + 1;
		for (int i = 0; i < count; i++) {
			int x = panelLeft + PANEL_PADDING + i * SLOT_PITCH + 1;
			// Slot background: dark edge top and left, light edge bottom and right.
			graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_LIGHT);
			graphics.fill(x - 1, y - 1, x + 16, y + 16, SLOT_SHADOW);
			graphics.fill(x, y, x + 16, y + 16, SLOT_COLOR);

			PreviewSlot slot = PREVIEW.get(i);
			if (slot.item().builtInRegistryHolder().areComponentsBound()) {
				ItemStack stack = new ItemStack(slot.item(), slot.count());
				graphics.fakeItem(stack, x, y);
				graphics.itemDecorations(font, stack, x, y);
			} else {
				graphics.fill(x + 3, y + 3, x + 13, y + 13, slot.placeholderColor());
			}
			if (slot.matches()) {
				HighlightRenderer.extractFrame(graphics, x, y, accent);
			} else {
				HighlightRenderer.extractDim(graphics, x, y, dim);
			}
		}
	}

	/** One of the red, green and blue parts of the highlight colour, 0 to 255. */
	private final class ChannelSlider extends AbstractSliderButton {
		private final Component label;
		private final int shift;

		ChannelSlider(int x, int y, Component label, int shift) {
			super(x, y, COLUMN_WIDTH, ROW_HEIGHT, Component.empty(), channelOf(highlightColor, shift) / 255.0);
			this.label = label;
			this.shift = shift;
			updateMessage();
		}

		private int channel() {
			return (int) Math.round(value * 255);
		}

		void sync() {
			setValue(channelOf(highlightColor, shift) / 255.0);
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.translatable(KEY + "value", label, channel()));
		}

		@Override
		protected void applyValue() {
			if (syncing) {
				return;
			}
			highlightColor = highlightColor & ~(0xFF << shift) | channel() << shift;
			syncing = true;
			try {
				if (hexField != null) {
					hexField.setValue(HighlightConfig.formatColor(highlightColor));
					hexField.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
				}
			} finally {
				syncing = false;
			}
		}
	}

	private static int channelOf(int rgb, int shift) {
		return rgb >> shift & 0xFF;
	}

	/** Opacity of the layer over non-matching slots, 0 to 100 percent. */
	private final class OpacitySlider extends AbstractSliderButton {
		OpacitySlider(int x, int y) {
			super(x, y, COLUMN_WIDTH, ROW_HEIGHT, Component.empty(), dimOpacity / 100.0);
			updateMessage();
		}

		private int percent() {
			return (int) Math.round(value * 100);
		}

		void sync() {
			setValue(dimOpacity / 100.0);
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.translatable(KEY + (compact ? "dimmed_slots_opacity" : "dim_opacity"), percent()));
		}

		@Override
		protected void applyValue() {
			if (!syncing) {
				dimOpacity = percent();
			}
		}
	}
}
