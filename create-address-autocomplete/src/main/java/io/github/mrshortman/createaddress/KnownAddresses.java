package io.github.mrshortman.createaddress;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * Collects the package addresses the client can see. There is no server part, so "can see" means the block
 * entities of the chunks the client has loaded right now.
 */
public final class KnownAddresses {
	// Create uses -1 for the gauge's own frogport and 0 for clipboard entries; ours come after those.
	/** Frogports and postboxes: places a package can actually arrive at. */
	private static final int RANK_PORT = 1;
	/** Signs on packagers: the address that packager writes onto its packages. */
	private static final int RANK_PACKAGER = 2;
	/** Addresses other gauges and redstone requesters send to; may point somewhere that is not loaded. */
	private static final int RANK_IN_USE = 3;

	private KnownAddresses() {
	}

	/** Appends every address that is not in {@code options} yet. */
	public static void addTo(List<IntAttached<String>> options) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null || minecraft.player == null) {
			return;
		}

		Set<String> ports = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		Set<String> packagers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		Set<String> inUse = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

		ChunkPos center = minecraft.player.chunkPosition();
		// The server never sends chunks further out than the client asked for.
		int radius = minecraft.options.renderDistance().get() + 1;
		for (int x = center.x() - radius; x <= center.x() + radius; x++) {
			for (int z = center.z() - radius; z <= center.z() + radius; z++) {
				LevelChunk chunk = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
				if (chunk == null) {
					continue;
				}
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					collect(blockEntity, ports, packagers, inUse);
				}
			}
		}

		Set<String> taken = new HashSet<>();
		for (IntAttached<String> option : options) {
			taken.add(option.getValue());
		}
		append(options, taken, ports, RANK_PORT);
		append(options, taken, packagers, RANK_PACKAGER);
		append(options, taken, inUse, RANK_IN_USE);
	}

	private static void collect(BlockEntity blockEntity, Set<String> ports, Set<String> packagers, Set<String> inUse) {
		switch (blockEntity) {
			case PackagePortBlockEntity port -> add(ports, port.addressFilter);
			case PackagerBlockEntity packager -> add(packagers, packager.signBasedAddress);
			case RedstoneRequesterBlockEntity requester -> add(inUse, requester.encodedTargetAdress);
			case FactoryPanelBlockEntity gauges -> {
				for (ServerFactoryPanelBehaviour gauge : gauges.panels.values()) {
					if (gauge.active) {
						add(inUse, gauge.recipeAddress);
					}
				}
			}
			default -> {
			}
		}
	}

	private static void add(Set<String> addresses, String address) {
		if (address != null && !address.isBlank()) {
			addresses.add(address.trim());
		}
	}

	private static void append(List<IntAttached<String>> options, Set<String> taken, Set<String> addresses, int rank) {
		for (String address : addresses) {
			if (taken.add(address)) {
				options.add(IntAttached.with(rank, address));
			}
		}
	}
}
