package io.github.mrshortman.jeihighlight;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;

/** Registered through the {@code jei_mod_plugin} entrypoint; only hands the JEI runtime to {@link JeiSearch}. */
public final class JeiHighlightPlugin implements IModPlugin {
	private static final Identifier UID = Identifier.fromNamespaceAndPath(JeiInventoryHighlight.MOD_ID, "plugin");

	@Override
	public Identifier getPluginUid() {
		return UID;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		JeiSearch.setRuntime(jeiRuntime);
	}

	@Override
	public void onRuntimeUnavailable() {
		JeiSearch.setRuntime(null);
	}
}
