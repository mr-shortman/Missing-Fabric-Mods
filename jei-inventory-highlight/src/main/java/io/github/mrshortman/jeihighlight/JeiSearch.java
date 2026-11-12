package io.github.mrshortman.jeihighlight;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

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

	/**
	 * Matches inside carried containers, per contents component. Those components are immutable, so their
	 * identity is a safe key; the map is dropped whenever the search changes.
	 */
	private static final Map<Object, Integer> matchesInside = new IdentityHashMap<>();
	private static final int MAX_CACHED_CONTAINERS = 1024;
	/** A shulker box may hold bundles; anything nested deeper than this is not worth following. */
	private static final int MAX_NESTING = 4;

	/** The JEI search as it applies to the stacks of an inventory. */
	public interface Matcher {
		/** @return true if the stack itself matches the search */
		boolean matches(ItemStack stack);

		/** @return how many stacks stored inside this stack (shulker box, bundle, ...) match the search */
		int matchesInside(ItemStack stack);
	}

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
		matchesInside.clear();

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
	 * @return the current JEI search, or null if there is nothing to highlight (JEI hidden or search bar empty).
	 */
	public static @Nullable Matcher currentMatcher() {
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
			matchesInside.clear();
		}
		return new Matcher() {
			@Override
			public boolean matches(ItemStack stack) {
				return JeiSearch.matches(rt, helper, stack);
			}

			@Override
			public int matchesInside(ItemStack stack) {
				return countInside(rt, helper, stack, 0);
			}
		};
	}

	private static int countInside(IJeiRuntime rt, IIngredientHelper<ItemStack> helper, ItemStack stack, int depth) {
		int count = 0;
		ItemContainerContents container = stack.get(DataComponents.CONTAINER);
		if (container != null) {
			count += countInside(rt, helper, container, container.nonEmptyItems(), depth);
		}
		BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
		if (bundle != null) {
			count += countInside(rt, helper, bundle, bundle.items(), depth);
		}
		return count;
	}

	private static int countInside(IJeiRuntime rt, IIngredientHelper<ItemStack> helper, Object contents, Iterable<ItemStackTemplate> items, int depth) {
		Integer cached = matchesInside.get(contents);
		if (cached != null) {
			return cached;
		}
		int count = 0;
		for (ItemStackTemplate template : items) {
			ItemStack inner = template.create();
			if (matches(rt, helper, inner)) {
				count++;
			}
			if (depth < MAX_NESTING) {
				count += countInside(rt, helper, inner, depth + 1);
			}
		}
		if (matchesInside.size() >= MAX_CACHED_CONTAINERS) {
			matchesInside.clear();
		}
		matchesInside.put(contents, count);
		return count;
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
