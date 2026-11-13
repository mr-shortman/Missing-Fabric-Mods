package io.github.mrshortman.createaddress;

import java.util.List;

import com.zurrtum.create.catnip.data.IntAttached;

/** Implemented on Create's {@code DestinationSuggestions} by a mixin. */
public interface AddressSuggestions {
	/** The options the box completes from; the number is the sort key, lower comes first. */
	List<IntAttached<String>> createAddress$options();

	/**
	 * Marks this list as belonging to an address box. Train schedules use the same class for station names and
	 * keep Create's behaviour.
	 */
	void createAddress$markAddressBox();
}
