package io.github.mrshortman.jeihighlight.mixin;

import io.github.mrshortman.jeihighlight.HighlightRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	/** After the screen (and with it JEI's overlay) has been drawn, but before the deferred tooltips. */
	@Inject(
		method = "extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
			shift = At.Shift.AFTER
		)
	)
	private void jeiInventoryHighlight$afterScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		HighlightRenderer.extractSearchFieldIndicator(graphics);
	}
}
