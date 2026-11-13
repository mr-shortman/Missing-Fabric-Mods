package io.github.mrshortman.createaddress;

import net.minecraft.client.input.MouseButtonEvent;

/** Implemented on Create's {@code AddressEditBox} by a mixin. */
public interface SuggestionPicker {
	/**
	 * Takes the suggestion under the mouse, if the box is focused and its list is open there.
	 *
	 * @return true if the click hit the list and must not reach anything else
	 */
	boolean createAddress$pickAt(MouseButtonEvent click);
}
