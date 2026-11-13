package io.github.mrshortman.createaddress.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.zurrtum.create.client.content.logistics.AddressEditBox;
import com.zurrtum.create.client.content.trains.schedule.DestinationSuggestions;

import io.github.mrshortman.createaddress.SuggestionPicker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

@Mixin(AddressEditBox.class)
public abstract class AddressEditBoxMixin extends EditBox implements SuggestionPicker {
	@Shadow
	@Final
	private DestinationSuggestions destinationSuggestions;

	private AddressEditBoxMixin(Font font, int x, int y, int width, int height, Component narration) {
		super(font, x, y, width, height, narration);
	}

	/** Enter takes the highlighted suggestion. Without an open list Create's Enter (leave the box) stays. */
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void createAddress$pickWithEnter(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!isFocused() || !event.isConfirmation()) {
			return;
		}
		CommandSuggestions.SuggestionsList list =
				((CommandSuggestionsAccessor) destinationSuggestions).createAddress$getList();
		if (list == null) {
			return;
		}
		list.useSuggestion();
		// Otherwise "Port1" would offer "Port10" right away and a second Enter would take that.
		destinationSuggestions.hide();
		cir.setReturnValue(true);
	}

	@Override
	public boolean createAddress$pickAt(MouseButtonEvent click) {
		if (!isFocused() || !destinationSuggestions.mouseClicked(click)) {
			return false;
		}
		destinationSuggestions.hide();
		return true;
	}
}
