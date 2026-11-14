package io.github.mrshortman.conveyormover.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;

import io.github.mrshortman.conveyormover.client.MovePreview;
import net.minecraft.client.Minecraft;

/** Create calls this once per client tick for its own chain preview; ours rides along. */
@Mixin(ChainConveyorConnectionHandler.class)
public abstract class ChainConveyorConnectionHandlerMixin {
	@Inject(method = "clientTick", at = @At("HEAD"))
	private static void conveyorMover$preview(Minecraft mc, CallbackInfo ci) {
		MovePreview.tick(mc);
	}
}
