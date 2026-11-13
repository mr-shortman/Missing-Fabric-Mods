package io.github.mrshortman.createaddress.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.CommandSuggestions;

@Mixin(CommandSuggestions.class)
public interface CommandSuggestionsAccessor {
	/** The open list, or null while nothing is shown. */
	@Accessor("suggestions")
	CommandSuggestions.SuggestionsList createAddress$getList();
}
