package io.github.mrshortman.createaddress.mixin;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.client.content.trains.schedule.DestinationSuggestions;

import io.github.mrshortman.createaddress.AddressSuggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;

@Mixin(DestinationSuggestions.class)
public abstract class DestinationSuggestionsMixin implements AddressSuggestions {
	@Shadow
	@Final
	private EditBox textBox;
	@Shadow
	@Final
	private List<IntAttached<String>> viableStations;
	@Shadow
	private String previous;
	@Shadow
	private boolean active;
	@Shadow
	List<Suggestion> currentSuggestions;

	@Unique
	private boolean createAddress$addressBox;

	@Shadow
	public abstract void showSuggestions(boolean narrateFirstSuggestion);

	@Override
	public List<IntAttached<String>> createAddress$options() {
		return viableStations;
	}

	@Override
	public void createAddress$markAddressBox() {
		createAddress$addressBox = true;
	}

	/**
	 * Create filters by the text left of the cursor with the selected text cut out. Clicking into a filled box
	 * selects everything, so the filter is empty and the whole list shows although the box has text in it. Here
	 * the filter is simply what the box contains.
	 */
	@Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
	private void createAddress$filterByWholeText(CallbackInfo ci) {
		if (!createAddress$addressBox) {
			return;
		}
		ci.cancel();

		if (!active) {
			((CommandSuggestions) (Object) this).hide();
			// Forget the last filter so the list comes back when the box is focused again.
			previous = null;
			return;
		}
		String value = textBox.getValue();
		if (value.equals(previous)) {
			// Nothing new was typed; leaves a list alone that was closed with Escape or by picking an entry.
			return;
		}
		previous = value;

		String prefix = value.toLowerCase(Locale.ROOT);
		currentSuggestions = viableStations.stream()
				.filter(option -> !option.getValue().equals(value)
						&& option.getValue().toLowerCase(Locale.ROOT).startsWith(prefix))
				.sorted(Comparator.comparingInt(IntAttached::getFirst))
				.map(option -> new Suggestion(new StringRange(0, 1000), option.getValue()))
				.toList();
		showSuggestions(false);
	}
}
