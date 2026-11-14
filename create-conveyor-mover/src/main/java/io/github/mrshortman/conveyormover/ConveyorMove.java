package io.github.mrshortman.conveyormover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectedPort;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorRoutingTable;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorRoutingTable.RoutingTableEntry;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.PackagePortTarget.ChainConveyorFrogportTarget;
import com.zurrtum.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Moves one chain conveyor to another block while keeping everything that hangs on it: its chains, the packages
 * on them, the routing tables and the frogports. {@link #plan} checks Create's own rules for the new spot and
 * changes nothing, so the client uses it for the preview as well.
 */
public final class ConveyorMove {
	private static final String KEY = "create_conveyor_mover.";

	/** A frogport whose link has to follow the move. */
	record PortChange(BlockPos portPos, BlockPos anchor, @Nullable BlockPos connection, float chainPos, Vec3 point) {
	}

	/** {@code error} is null when the move is allowed. */
	public record Plan(BlockPos from, BlockPos to, List<BlockPos> neighbours, List<PortChange> ports, int chainDelta,
			@Nullable Component error) {
		public boolean valid() {
			return error == null;
		}
	}

	private ConveyorMove() {
	}

	public static Plan plan(Level level, BlockPos from, BlockPos to, Player player) {
		if (!(level.getBlockEntity(from) instanceof ChainConveyorBlockEntity conveyor)) {
			return fail(from, to, Component.translatable(KEY + "gone"));
		}
		BlockState state = conveyor.getBlockState();
		if (from.equals(to) || !level.isInWorldBounds(to) || !level.getBlockState(to).canBeReplaced()
				|| !level.isUnobstructed(state, to, CollisionContext.empty())) {
			return fail(from, to, Component.translatable(KEY + "obstructed"));
		}
		// Create refuses to place a conveyor directly next to another one.
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockPos near = to.offset(x, 0, z);
				if (!near.equals(from) && level.getBlockState(near).is(AllBlocks.CHAIN_CONVEYOR)) {
					return fail(from, to, Component.translatable(KEY + "next_to_conveyor"));
				}
			}
		}

		int maxLength = AllConfigs.server().kinetics.maxChainConveyorLength.get();
		List<BlockPos> neighbours = new ArrayList<>();
		int chainDelta = 0;
		for (BlockPos connection : conveyor.connections) {
			BlockPos neighbour = from.offset(connection);
			if (!level.isLoaded(neighbour) || !(level.getBlockEntity(neighbour) instanceof ChainConveyorBlockEntity other)
					|| !other.connections.contains(connection.multiply(-1))) {
				return fail(from, to, Component.translatable(KEY + "neighbour_missing", neighbour.toShortString()));
			}
			String problem = connectionProblem(to, neighbour, maxLength);
			if (problem != null) {
				return fail(from, to, Component.translatable(problem));
			}
			neighbours.add(neighbour);
			chainDelta += ChainConveyorBlockEntity.getChainCost(neighbour.subtract(to))
					- ChainConveyorBlockEntity.getChainCost(connection);
		}

		List<PortChange> ports = new ArrayList<>();
		double portRange = AllConfigs.server().logistics.packagePortRange.get();
		for (PackagePortBlockEntity port : portsAround(level, from, to, neighbours, Mth.ceil(portRange) + 3)) {
			if (!(port.target instanceof ChainConveyorFrogportTarget target)) {
				continue;
			}
			BlockPos portPos = port.getBlockPos();
			PortChange change = follow(level, conveyor, from, to, portPos, target);
			if (change == null) {
				continue;
			}
			Vec3 reach = change.point().subtract(Vec3.atBottomCenterOf(portPos));
			if (reach.y < 0) {
				return fail(from, to, Component.translatable(KEY + "frogport_above", portPos.toShortString()));
			}
			if (reach.length() > portRange) {
				return fail(from, to, Component.translatable(KEY + "frogport_too_far", portPos.toShortString()));
			}
			ports.add(change);
		}

		if (chainDelta > 0 && !player.isCreative() && !ChainConveyorBlockEntity.getChainsFromInventory(player,
				new ItemStack(Items.IRON_CHAIN), chainDelta, true)) {
			return new Plan(from, to, neighbours, ports, chainDelta,
					Component.translatable("create.chain_conveyor.not_enough_chains"));
		}
		return new Plan(from, to, neighbours, ports, chainDelta, null);
	}

	private static Plan fail(BlockPos from, BlockPos to, Component error) {
		return new Plan(from, to, List.of(), List.of(), 0, error);
	}

	/** The checks Create runs when two conveyors get connected with chains. Returns a translation key or null. */
	static @Nullable String connectionProblem(BlockPos a, BlockPos b, int maxLength) {
		if (!b.closerThan(a, maxLength)) {
			return "create.chain_conveyor.too_far";
		}
		if (b.closerThan(a, 2.5)) {
			return "create.chain_conveyor.too_close";
		}
		Vec3 diff = Vec3.atLowerCornerOf(b.subtract(a));
		double horizontalDistance = diff.multiply(1, 0, 1).length() - 1.5;
		if (horizontalDistance <= 0) {
			return "create.chain_conveyor.cannot_connect_vertically";
		}
		if (Math.abs(diff.y) / horizontalDistance > 1) {
			return "create.chain_conveyor.too_steep";
		}
		return null;
	}

	/** Where the frogport's link ends up after the move, or null if the move does not touch it. */
	private static @Nullable PortChange follow(Level level, ChainConveyorBlockEntity conveyor, BlockPos from, BlockPos to,
			BlockPos portPos, ChainConveyorFrogportTarget target) {
		BlockPos anchor = portPos.offset(target.relativePos);
		BlockPos connection = target.connection;
		if (anchor.equals(from)) {
			if (connection == null) {
				// Sits on the wheel itself: same angle around the moved conveyor.
				return new PortChange(portPos, to, null, target.chainPos, Vec3.atBottomCenterOf(to)
						.add(VecHelper.rotate(new Vec3(0, 6 / 16f, 0.875), target.chainPos, Axis.Y)));
			}
			if (!conveyor.connections.contains(connection)) {
				return null;
			}
			boolean reversed = conveyor.getSpeed() < 0;
			return slide(portPos, stats(from, connection, reversed), to, from.offset(connection).subtract(to), reversed,
					target.chainPos);
		}
		if (connection != null && anchor.offset(connection).equals(from) && conveyor.connections.contains(
				connection.multiply(-1)) && level.getBlockEntity(anchor) instanceof ChainConveyorBlockEntity other) {
			boolean reversed = other.getSpeed() < 0;
			return slide(portPos, stats(anchor, connection, reversed), anchor, to.subtract(anchor), reversed,
					target.chainPos);
		}
		return null;
	}

	/** Keeps a port at the spot of the new chain that is closest to where it was on the old one. */
	private static PortChange slide(BlockPos portPos, ConnectionStats oldChain, BlockPos anchor, BlockPos connection,
			boolean reversed, float chainPos) {
		Vec3 oldDirection = oldChain.end().subtract(oldChain.start()).normalize();
		Vec3 oldPoint = oldChain.start().add(oldDirection.scale(Math.min(oldChain.chainLength(), chainPos)));
		ConnectionStats newChain = stats(anchor, connection, reversed);
		Vec3 newDirection = newChain.end().subtract(newChain.start()).normalize();
		float newPos = (float) Mth.clamp(oldPoint.subtract(newChain.start()).dot(newDirection), 0,
				newChain.chainLength());
		return new PortChange(portPos, anchor, connection, newPos, newChain.start().add(newDirection.scale(newPos)));
	}

	/** Same geometry as ChainConveyorBlockEntity#calculateConnectionStats, for a conveyor that is not there yet. */
	static ConnectionStats stats(BlockPos pos, BlockPos connection, boolean reversed) {
		float offBranchDistance = 35f;
		float direction = Mth.RAD_TO_DEG * (float) Mth.atan2(connection.getX(), connection.getZ());
		float angle = wrapAngle(direction - offBranchDistance * (reversed ? -1 : 1));
		float oppositeAngle = wrapAngle(angle + 180 + 2 * offBranchDistance * (reversed ? -1 : 1));
		Vec3 start = Vec3.atBottomCenterOf(pos).add(VecHelper.rotate(new Vec3(0, 0, 1.25), angle, Axis.Y))
				.add(0, 6 / 16f, 0);
		Vec3 end = Vec3.atBottomCenterOf(pos.offset(connection))
				.add(VecHelper.rotate(new Vec3(0, 0, 1.25), oppositeAngle, Axis.Y)).add(0, 6 / 16f, 0);
		return new ConnectionStats(angle, (float) start.distanceTo(end), start, end);
	}

	private static float wrapAngle(float angle) {
		angle %= 360;
		return angle < 0 ? angle + 360 : angle;
	}

	/** Frogports and postboxes in the loaded chunks around the conveyors that take part in the move. */
	private static List<PackagePortBlockEntity> portsAround(Level level, BlockPos from, BlockPos to,
			List<BlockPos> neighbours, int margin) {
		int minX = Math.min(from.getX(), to.getX());
		int maxX = Math.max(from.getX(), to.getX());
		int minZ = Math.min(from.getZ(), to.getZ());
		int maxZ = Math.max(from.getZ(), to.getZ());
		for (BlockPos neighbour : neighbours) {
			minX = Math.min(minX, neighbour.getX());
			maxX = Math.max(maxX, neighbour.getX());
			minZ = Math.min(minZ, neighbour.getZ());
			maxZ = Math.max(maxZ, neighbour.getZ());
		}
		List<PackagePortBlockEntity> ports = new ArrayList<>();
		for (int chunkX = (minX - margin) >> 4; chunkX <= (maxX + margin) >> 4; chunkX++) {
			for (int chunkZ = (minZ - margin) >> 4; chunkZ <= (maxZ + margin) >> 4; chunkZ++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
				if (chunk == null) {
					continue;
				}
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					if (blockEntity instanceof PackagePortBlockEntity port) {
						ports.add(port);
					}
				}
			}
		}
		return ports;
	}

	/** Carries out a valid plan. Returns false if the conveyor could not be set down. */
	public static boolean execute(ServerLevel level, Plan plan, Player player) {
		BlockPos from = plan.from();
		BlockPos to = plan.to();
		if (!plan.valid() || !(level.getBlockEntity(from) instanceof ChainConveyorBlockEntity old)) {
			return false;
		}
		BlockState state = old.getBlockState();
		Set<BlockPos> connections = new HashSet<>(old.connections);
		List<ChainConveyorPackage> looping = new ArrayList<>(old.getLoopingPackages());
		Map<BlockPos, List<ChainConveyorPackage>> travelling = new HashMap<>(old.getTravellingPackages());
		List<RoutingTableEntry> routes = new ArrayList<>(old.routingTable.entriesByDistance);
		boolean reversed = old.reversed;

		// Conveyors that were driven through this one look for another source while its chains are still known;
		// the conveyor picks the rotation up again from its chains (or a shaft) once it stands at the new place.
		old.detachKinetics();
		old.removeSource();
		// Emptied first, so that removing the block neither drops chains and packages nor cuts the neighbours loose.
		old.clearContent();
		level.removeBlock(from, false);
		level.setBlock(to, state, Block.UPDATE_ALL);
		if (!(level.getBlockEntity(to) instanceof ChainConveyorBlockEntity moved)) {
			return false;
		}

		// A connection is the offset to the other conveyor, so every one of them shifts by the distance moved.
		BlockPos shift = from.subtract(to);
		moved.reversed = reversed;
		for (BlockPos connection : connections) {
			moved.connections.add(connection.offset(shift));
		}
		moved.getLoopingPackages().addAll(looping);
		travelling.forEach((connection, boxes) -> moved.getTravellingPackages().put(connection.offset(shift), boxes));
		for (RoutingTableEntry route : routes) {
			BlockPos next = route.nextConnection();
			moved.routingTable.entriesByDistance.add(connections.contains(next) ? redirect(route, next.offset(shift))
					: route);
		}
		moved.connectionStats = null;

		for (BlockPos neighbourPos : plan.neighbours()) {
			if (!(level.getBlockEntity(neighbourPos) instanceof ChainConveyorBlockEntity neighbour)) {
				continue;
			}
			BlockPos oldBack = from.subtract(neighbourPos);
			BlockPos newBack = to.subtract(neighbourPos);
			neighbour.connections.remove(oldBack);
			neighbour.connections.add(newBack);
			List<ChainConveyorPackage> boxes = neighbour.getTravellingPackages().remove(oldBack);
			if (boxes != null) {
				neighbour.getTravellingPackages().put(newBack, boxes);
			}
			ChainConveyorRoutingTable table = neighbour.routingTable;
			table.entriesByDistance.replaceAll(route -> oldBack.equals(route.nextConnection()) ? redirect(route, newBack)
					: route);
			table.changed = true;
			neighbour.travelPorts.values().removeIf(port -> oldBack.equals(port.connection()));
			neighbour.connectionStats = null;
			neighbour.notifyUpdate();
		}
		moved.routingTable.changed = true;
		moved.updateSpeed = true;
		moved.notifyUpdate();

		for (PortChange change : plan.ports()) {
			if (!(level.getBlockEntity(change.portPos()) instanceof PackagePortBlockEntity port)
					|| !(port.target instanceof ChainConveyorFrogportTarget target)) {
				continue;
			}
			target.relativePos = change.anchor().subtract(change.portPos());
			target.connection = change.connection();
			target.chainPos = change.chainPos();
			port.notifyUpdate();
			String filter = port.getFilterString();
			if (filter != null && level.getBlockEntity(change.anchor()) instanceof ChainConveyorBlockEntity anchor) {
				// The port would register itself on its next lazy tick; doing it now leaves no gap in the routing.
				(change.connection() == null ? anchor.loopPorts : anchor.travelPorts).put(
						change.portPos().subtract(change.anchor()),
						new ConnectedPort(change.chainPos(), change.connection(), filter));
			}
		}

		if (!player.isCreative()) {
			if (plan.chainDelta() > 0) {
				ChainConveyorBlockEntity.getChainsFromInventory(player, new ItemStack(Items.IRON_CHAIN),
						plan.chainDelta(), false);
			}
			for (int refund = -plan.chainDelta(); refund > 0; refund -= 64) {
				player.getInventory().placeItemBackInInventory(new ItemStack(Items.IRON_CHAIN, Math.min(refund, 64)));
			}
		}
		return true;
	}

	private static RoutingTableEntry redirect(RoutingTableEntry route, BlockPos nextConnection) {
		return new RoutingTableEntry(route.port(), route.distance(), nextConnection, route.timeout(),
				route.endOfRoute());
	}
}
