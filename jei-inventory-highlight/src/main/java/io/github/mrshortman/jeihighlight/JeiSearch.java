package io.github.mrshortman.jeihighlight;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Everything this mod needs from JEI: where the search bar is and which items the current search matches.
 *
 * <p>The matching itself is left to JEI (via {@code IIngredientFilter}), so {@code @mod}, {@code #tag},
 * tooltip search and so on behave exactly like in the JEI item list.
 */
public final class JeiSearch {
	private static @Nullable IJeiRuntime runtime;

	// JEI's API does not expose the search bar widget, so these two are looked up reflectively on the overlay.
	private static @Nullable EditBox searchField;
	private static @Nullable Method isSearchDisplayed;

	private static @Nullable String cachedFilterText;
	private static Set<Item> matchingItems = Set.of();
	private static Set<Object> matchingSubtypes = Set.of();
	private static @Nullable Set<Object> knownSubtypes;

	private JeiSearch() {
	}

	static void setRuntime(@Nullable IJeiRuntime newRuntime) {
		runtime = newRuntime;
		searchField = null;
		isSearchDisplayed = null;
		cachedFilterText = null;
		matchingItems = Set.of();
		matchingSubtypes = Set.of();
		knownSubtypes = null;

		if (newRuntime != null) {
			inspectOverlay(newRuntime.getIngredientListOverlay());
		}
	}

	private static void inspectOverlay(IIngredientListOverlay overlay) {
		try {
			for (Class<?> type = overlay.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
				for (Field field : type.getDeclaredFields()) {
					if (searchField == null && !Modifier.isStatic(field.getModifiers()) && EditBox.class.isAssignableFrom(field.getType())) {
						field.setAccessible(true);
						searchField = (EditBox) field.get(overlay);
					}
				}
				for (Method method : type.getDeclaredMethods()) {
					if (isSearchDisplayed == null && method.getName().equals("isSearchDisplayed")
						&& method.getParameterCount() == 0 && method.getReturnType() == boolean.class) {
						method.setAccessible(true);
						isSearchDisplayed = method;
					}
				}
			}
		} catch (ReflectiveOperationException | RuntimeException e) {
			JeiInventoryHighlight.LOGGER.warn("Could not inspect the JEI overlay", e);
		}
		if (searchField == null) {
			JeiInventoryHighlight.LOGGER.warn("JEI search bar not found, double-click toggling is unavailable with this JEI version");
		}
	}

	/** The JEI search bar widget, or null while JEI is not showing it on the current screen. */
	public static @Nullable EditBox visibleSearchField() {
		IJeiRuntime rt = runtime;
		EditBox field = searchField;
		if (rt == null || field == null || !isSearchVisible(rt)) {
			return null;
		}
		return field;
	}

	public static boolean isOverSearchField(double mouseX, double mouseY) {
		EditBox field = visibleSearchField();
		return field != null && field.isMouseOver(mouseX, mouseY);
	}

	private static boolean isSearchVisible(IJeiRuntime rt) {
		IIngredientListOverlay overlay = rt.getIngredientListOverlay();
		Method method = isSearchDisplayed;
		if (method != null) {
			try {
				return (boolean) method.invoke(overlay);
			} catch (ReflectiveOperationException | RuntimeException e) {
				JeiInventoryHighlight.LOGGER.warn("Could not query the JEI search bar, falling back to the item list", e);
				isSearchDisplayed = null;
			}
		}
		return overlay.isListDisplayed();
	}

	/**
	 * @return a test for "does this stack match the JEI search", or null if there is nothing to highlight
	 * (JEI hidden or search bar empty).
	 */
	public static @Nullable Predicate<ItemStack> currentMatcher() {
		IJeiRuntime rt = runtime;
		if (rt == null || !isSearchVisible(rt)) {
			return null;
		}
		String filterText = rt.getIngredientFilter().getFilterText();
		if (filterText.isBlank()) {
			return null;
		}

		IIngredientHelper<ItemStack> helper = rt.getIngredientManager().getIngredientHelper(VanillaTypes.ITEM_STACK);
		if (!filterText.equals(cachedFilterText)) {
			Set<Item> items = new HashSet<>();
			matchingSubtypes = collectSubtypes(helper, rt.getIngredientFilter().getFilteredItemStacks(), items);
			matchingItems = items;
			cachedFilterText = filterText;
		}
		return stack -> matches(rt, helper, stack);
	}

	private static boolean matches(IJeiRuntime rt, IIngredientHelper<ItemStack> helper, ItemStack stack) {
		if (stack.isEmpty() || !matchingItems.contains(stack.getItem())) {
			return false;
		}
		if (!helper.hasSubtypes(stack)) {
			return true;
		}
		// Potions, enchanted books etc. share one Item, so compare the exact variant.
		Object subtype = helper.getUid(stack, UidContext.Ingredient);
		if (matchingSubtypes.contains(subtype)) {
			return true;
		}
		// A variant JEI does not list (e.g. a book with several enchantments) cannot be searched for,
		// so it counts as a match whenever its item does.
		Set<Object> known = knownSubtypes;
		if (known == null) {
			known = collectSubtypes(helper, rt.getIngredientManager().getAllItemStacks(), null);
			knownSubtypes = known;
		}
		return !known.contains(subtype);
	}

	private static Set<Object> collectSubtypes(IIngredientHelper<ItemStack> helper, Collection<ItemStack> stacks, @Nullable Set<Item> itemsOut) {
		Set<Object> subtypes = new HashSet<>();
		for (ItemStack stack : stacks) {
			if (itemsOut != null) {
				itemsOut.add(stack.getItem());
			}
			if (helper.hasSubtypes(stack)) {
				subtypes.add(helper.getUid(stack, UidContext.Ingredient));
			}
		}
		return subtypes;
	}
}
