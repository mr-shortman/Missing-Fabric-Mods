package io.github.mrshortman.conveyormover;

import org.jspecify.annotations.Nullable;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;

import io.github.mrshortman.conveyormover.Selections.Selection;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

/**
 * The two wrench clicks: the first one on a chain conveyor picks it up, the second one sets it down. Runs on both
 * sides; the server does the work and reports back, the client only keeps its selection in step for the preview.
 */
public final class WrenchMove {
	private WrenchMove() {
	}

	/** Returns null when the click is none of our business and the wrench should do what it always does. */
	public static @Nullable InteractionResult onWrenchUsed(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !player.mayBuild()) {
			return null;
		}
		Level level = context.getLevel();
		BlockPos clicked = context.getClickedPos();
		Selection selection = Selections.get(player);

		if (selection == null) {
			// Sneaking stays Create's "dismantle" click.
			if (player.isShiftKeyDown() || !level.getBlockState(clicked).is(AllBlocks.CHAIN_CONVEYOR)) {
				return null;
			}
			Selections.set(player, clicked);
			status(player, Component.translatable("create_conveyor_mover.selected"));
			return InteractionResult.SUCCESS;
		}

		Selections.clear(player);
		if (player.isShiftKeyDown() || selection.dimension() != level.dimension() || clicked.equals(selection.pos())) {
			status(player, Component.translatable("create_conveyor_mover.cancelled"));
			return InteractionResult.SUCCESS;
		}
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		ConveyorMove.Plan plan = ConveyorMove.plan(level, selection.pos(), destination(level, context), player);
		Component error = plan.error();
		if (error == null && !ConveyorMove.execute(serverLevel, plan, player)) {
			error = Component.translatable("create_conveyor_mover.obstructed");
		}
		if (error != null) {
			status(player, error.copy().withStyle(ChatFormatting.RED));
			AllSoundEvents.DENY.playOnServer(level, clicked);
			return InteractionResult.SUCCESS;
		}
		status(player, Component.translatable("create_conveyor_mover.moved").withStyle(ChatFormatting.GREEN));
		SoundType chain = Blocks.IRON_CHAIN.defaultBlockState().getSoundType();
		level.playSound(null, plan.to(), chain.getPlaceSound(), SoundSource.BLOCKS, (chain.getVolume() + 1) / 2,
				chain.getPitch() * 0.8f);
		return InteractionResult.SUCCESS;
	}

	/** Like placing a block: into the clicked block if it gives way (grass, snow), otherwise against its face. */
	public static BlockPos destination(Level level, UseOnContext context) {
		return destination(level, context.getClickedPos(), context.getClickedFace());
	}

	public static BlockPos destination(Level level, BlockPos clicked, Direction face) {
		return level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(face);
	}

	private static void status(Player player, Component message) {
		if (!player.level().isClientSide()) {
			player.sendOverlayMessage(message);
		}
	}
}
