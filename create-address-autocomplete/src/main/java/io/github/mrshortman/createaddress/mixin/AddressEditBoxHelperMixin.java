package io.github.mrshortman.createaddress.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.zurrtum.create.client.content.logistics.AddressEditBoxHelper;
import com.zurrtum.create.client.content.trains.schedule.DestinationSuggestions;

import io.github.mrshortman.createaddress.AddressSuggestions;
import io.github.mrshortman.createaddress.KnownAddresses;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

/**
 * Create fills the address box's completion list from clipboards only. Every address box (factory gauge,
 * redstone requester, stock keeper, package filter) gets its list here, so this is the one place to add more.
 */
@Mixin(AddressEditBoxHelper.class)
public abstract class AddressEditBoxHelperMixin {
	@Inject(method = "createSuggestions", at = @At("RETURN"))
	private static void createAddress$addKnownAddresses(Screen screen, EditBox input, boolean anchorToBottom,
			String localAddress, CallbackInfoReturnable<DestinationSuggestions> cir) {
		AddressSuggestions suggestions = (AddressSuggestions) cir.getReturnValue();
		suggestions.createAddress$markAddressBox();
		KnownAddresses.addTo(suggestions.createAddress$options());
	}
}
