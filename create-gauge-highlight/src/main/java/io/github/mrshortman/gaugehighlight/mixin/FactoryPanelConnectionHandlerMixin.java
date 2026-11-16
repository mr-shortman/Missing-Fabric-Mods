package io.github.mrshortman.gaugehighlight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelConnectionHandler;

import io.github.mrshortman.gaugehighlight.GaugeHighlight;
import net.minecraft.client.Minecraft;

/**
 * Create calls this once per client tick for its own gauge outlines; ours ride along. Running first lets Create's
 * action bar text win while a connection is being made.
 */
@Mixin(FactoryPanelConnectionHandler.class)
public abstract class FactoryPanelConnectionHandlerMixin {
	@Inject(method = "clientTick", at = @At("HEAD"))
	private static void gaugeHighlight$highlight(Minecraft mc, CallbackInfo ci) {
		GaugeHighlight.tick(mc);
	}
}
