package io.github.mrshortman.createaddress.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.mrshortman.createaddress.SuggestionPicker;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * A screen only passes a click to the widget under the mouse, and the suggestion list hangs outside its address
 * box. So the box never hears about clicks on its list; the gauge screen even unfocuses the box and may hit one
 * of its own buttons underneath. The click is therefore offered to the open list before the screen gets it.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@WrapOperation(method = "onButton", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
	private boolean createAddress$clickSuggestionFirst(Screen screen, MouseButtonEvent click, boolean doubled,
			Operation<Boolean> original) {
		if (click.button() == 0) {
			for (GuiEventListener child : screen.children()) {
				if (child instanceof SuggestionPicker picker && picker.createAddress$pickAt(click)) {
					return true;
				}
			}
		}
		return original.call(screen, click, doubled);
	}
}
