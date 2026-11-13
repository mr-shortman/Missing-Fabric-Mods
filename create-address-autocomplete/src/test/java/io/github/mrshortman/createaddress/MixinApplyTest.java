package io.github.mrshortman.createaddress;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.mrshortman.createaddress.mixin.DestinationSuggestionsAccessor;

/**
 * Runs inside Fabric Loader with Create Fly on the classpath. Loading the two Create classes applies our
 * mixins; if Create renamed something they fail to apply and so does this test.
 */
class MixinApplyTest {
	@Test
	void mixinsApplyToCreate() throws ClassNotFoundException {
		ClassLoader loader = getClass().getClassLoader();

		Class<?> suggestions = Class.forName(
				"com.zurrtum.create.client.content.trains.schedule.DestinationSuggestions", false, loader);
		assertTrue(DestinationSuggestionsAccessor.class.isAssignableFrom(suggestions));

		Class<?> helper = Class.forName(
				"com.zurrtum.create.client.content.logistics.AddressEditBoxHelper", false, loader);
		assertTrue(Arrays.stream(helper.getDeclaredMethods())
				.anyMatch(method -> method.getName().contains("createAddress$addKnownAddresses")));
	}
}
