package io.github.mrshortman.createaddress.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.client.content.trains.schedule.DestinationSuggestions;

@Mixin(DestinationSuggestions.class)
public interface DestinationSuggestionsAccessor {
	/** The options the box completes from; the number is the sort key, lower comes first. */
	@Accessor("viableStations")
	List<IntAttached<String>> createAddress$getOptions();
}
