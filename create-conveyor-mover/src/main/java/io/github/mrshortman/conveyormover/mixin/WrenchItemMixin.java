package io.github.mrshortman.conveyormover.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.zurrtum.create.content.equipment.wrench.WrenchItem;

import io.github.mrshortman.conveyormover.WrenchMove;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

@Mixin(WrenchItem.class)
public abstract class WrenchItemMixin {
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void conveyorMover$moveConveyor(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		InteractionResult result = WrenchMove.onWrenchUsed(context);
		if (result != null) {
			cir.setReturnValue(result);
		}
	}
}
