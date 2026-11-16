package io.github.mrshortman.gaugehighlight;

import java.util.ArrayList;
import java.util.List;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * While the player looks at a factory gauge with a wrench in hand, makes every gauge on the same wall blink that
 * has the looked-at gauge's item linked in as an input.
 */
public final class GaugeHighlight {
	/** How far along the wall to look. Create links gauges up to 16 blocks apart; this covers a chain of two. */
	static final int RANGE = 32;

	private static final int HOVERED = 0xEEEEEE;
	private static final int BLINK_ON = 0xFFD23F;
	private static final int BLINK_OFF = 0xC2571A;

	private GaugeHighlight() {
	}

	public static void tick(Minecraft mc) {
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;
		if (player == null || level == null) {
			return;
		}
		if (!player.getMainHandItem().is(AllItems.WRENCH) && !player.getOffhandItem().is(AllItems.WRENCH)) {
			return;
		}
		if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
			return;
		}
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof FactoryPanelBlock)) {
			return;
		}
		PanelSlot slot = FactoryPanelBlock.getTargetedSlot(pos, state, hit.getLocation());
		ServerFactoryPanelBehaviour hovered = ServerFactoryPanelBehaviour.at(level, new FactoryPanelPosition(pos, slot));
		if (hovered == null || hovered.getFilter().isEmpty()) {
			return;
		}

		List<ServerFactoryPanelBehaviour> users = users(level, hovered);
		boolean on = AnimationTickHolder.getTicks() % 12 < 6;
		for (ServerFactoryPanelBehaviour user : users) {
			outline(user, on ? BLINK_ON : BLINK_OFF, on ? 1 / 16f : 1 / 32f);
		}
		outline(hovered, HOVERED, 1 / 32f);

		Component item = hovered.getFilter().getHoverName();
		player.sendOverlayMessage(users.isEmpty()
				? Component.translatable("create_gauge_highlight.unused", item).withStyle(ChatFormatting.GRAY)
				: Component.translatable(
						users.size() == 1 ? "create_gauge_highlight.used_once" : "create_gauge_highlight.used",
						item, users.size()).withStyle(ChatFormatting.GOLD));
	}

	/** The gauges on the wall of {@code hovered} that take its item from it or from another gauge of that item. */
	static List<ServerFactoryPanelBehaviour> users(ClientLevel level, ServerFactoryPanelBehaviour hovered) {
		ItemStack item = hovered.getFilter();
		FactoryPanelPosition hoveredAt = hovered.getPanelPosition();
		BlockPos origin = hoveredAt.pos();
		Direction wall = FactoryPanelBlock.connectedDirection(hovered.blockEntity.getBlockState());

		List<ServerFactoryPanelBehaviour> users = new ArrayList<>();
		for (int chunkX = (origin.getX() - RANGE) >> 4; chunkX <= (origin.getX() + RANGE) >> 4; chunkX++) {
			for (int chunkZ = (origin.getZ() - RANGE) >> 4; chunkZ <= (origin.getZ() + RANGE) >> 4; chunkZ++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
				if (chunk == null) {
					continue;
				}
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					if (!(blockEntity instanceof FactoryPanelBlockEntity gauges) || blockEntity.isRemoved()
							|| !(gauges.getBlockState().getBlock() instanceof FactoryPanelBlock)
							|| !sameWall(origin, wall, gauges.getBlockPos(),
									FactoryPanelBlock.connectedDirection(gauges.getBlockState()))) {
						continue;
					}
					for (ServerFactoryPanelBehaviour gauge : gauges.panels.values()) {
						if (gauge != hovered && gauge.isActive() && takes(level, gauge, hoveredAt, item)) {
							users.add(gauge);
						}
					}
				}
			}
		}
		return users;
	}

	private static boolean takes(ClientLevel level, ServerFactoryPanelBehaviour gauge, FactoryPanelPosition hoveredAt,
			ItemStack item) {
		for (FactoryPanelConnection input : gauge.targetedBy.values()) {
			if (input.from.equals(hoveredAt)) {
				return true;
			}
			ServerFactoryPanelBehaviour source = ServerFactoryPanelBehaviour.at(level, input.from);
			if (source != null && ItemStack.isSameItemSameComponents(source.getFilter(), item)) {
				return true;
			}
		}
		return false;
	}

	/** Create's own idea of "the same wall": gauges facing the same way, in one plane, here limited to RANGE. */
	static boolean sameWall(BlockPos origin, Direction originFacing, BlockPos other, Direction otherFacing) {
		if (originFacing != otherFacing) {
			return false;
		}
		BlockPos diff = other.subtract(origin);
		if (originFacing.getAxis().choose(diff.getX(), diff.getY(), diff.getZ()) != 0) {
			return false;
		}
		return Math.abs(diff.getX()) <= RANGE && Math.abs(diff.getY()) <= RANGE && Math.abs(diff.getZ()) <= RANGE;
	}

	private static void outline(ServerFactoryPanelBehaviour gauge, int color, float width) {
		FactoryPanelPosition at = gauge.getPanelPosition();
		Outliner.getInstance()
				.showAABB(new Key(at), FactoryPanelConnectionHandler.getBB(gauge.blockEntity.getBlockState(), at))
				.colored(color).disableLineNormals().lineWidth(width);
	}

	/** Keeps our outlines apart from the ones Create keys by the bare gauge position. */
	private record Key(FactoryPanelPosition gauge) {
	}
}
