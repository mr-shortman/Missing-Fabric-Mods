package io.github.mrshortman.jeihighlight.mixin;

import io.github.mrshortman.jeihighlight.HighlightRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
	/**
	 * Right after the slots: on top of the items, but still below the hover highlight, the carried item
	 * and tooltips.
	 */
	@Inject(
		method = "extractContents(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlots(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
			shift = At.Shift.AFTER
		)
	)
	private void jeiInventoryHighlight$afterSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
		HighlightRenderer.extractSlotOverlay(graphics, screen.getMenu().slots);
	}
}
