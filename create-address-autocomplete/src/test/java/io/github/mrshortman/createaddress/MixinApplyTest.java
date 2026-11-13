package io.github.mrshortman.createaddress;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.mrshortman.createaddress.mixin.CommandSuggestionsAccessor;

/**
 * Runs inside Fabric Loader with Create Fly on the classpath. Loading the target classes applies our mixins;
 * if Create or Minecraft renamed something they fail to apply and so does this test.
 */
class MixinApplyTest {
	@Test
	void mixinsApply() throws ClassNotFoundException {
		assertTrue(AddressSuggestions.class.isAssignableFrom(
				load("com.zurrtum.create.client.content.trains.schedule.DestinationSuggestions")));
		assertTrue(SuggestionPicker.class.isAssignableFrom(
				load("com.zurrtum.create.client.content.logistics.AddressEditBox")));
		assertTrue(CommandSuggestionsAccessor.class.isAssignableFrom(
				load("net.minecraft.client.gui.components.CommandSuggestions")));
		assertTrue(hasMethod(load("com.zurrtum.create.client.content.logistics.AddressEditBoxHelper"),
				"createAddress$addKnownAddresses"));
		assertTrue(hasMethod(load("net.minecraft.client.MouseHandler"), "createAddress$clickSuggestionFirst"));
	}

	private Class<?> load(String name) throws ClassNotFoundException {
		return Class.forName(name, false, getClass().getClassLoader());
	}

	private static boolean hasMethod(Class<?> type, String namePart) {
		return Arrays.stream(type.getDeclaredMethods()).anyMatch(method -> method.getName().contains(namePart));
	}
}
