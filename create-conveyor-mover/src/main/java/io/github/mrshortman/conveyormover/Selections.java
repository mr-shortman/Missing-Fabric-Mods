package io.github.mrshortman.conveyormover;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The conveyor each player has picked up with the wrench. Client and server keep their own copy, because in
 * singleplayer both run in the same JVM; the client's copy only drives the preview.
 */
public final class Selections {
	public record Selection(ResourceKey<Level> dimension, BlockPos pos) {
	}

	private static final Map<UUID, Selection> SERVER = new ConcurrentHashMap<>();
	private static final Map<UUID, Selection> CLIENT = new ConcurrentHashMap<>();

	private Selections() {
	}

	private static Map<UUID, Selection> of(Player player) {
		return player.level().isClientSide() ? CLIENT : SERVER;
	}

	public static @Nullable Selection get(Player player) {
		return of(player).get(player.getUUID());
	}

	public static void set(Player player, BlockPos pos) {
		of(player).put(player.getUUID(), new Selection(player.level().dimension(), pos.immutable()));
	}

	public static void clear(Player player) {
		of(player).remove(player.getUUID());
	}
}
