package io.github.mrshortman.conveyormover.client;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;

import io.github.mrshortman.conveyormover.ConveyorMove;
import io.github.mrshortman.conveyormover.Selections;
import io.github.mrshortman.conveyormover.Selections.Selection;
import io.github.mrshortman.conveyormover.WrenchMove;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Outlines the picked-up conveyor and, at the block the player looks at, where it and its chains would end up. */
public final class MovePreview {
	private static final int NEUTRAL = 0xFFFFFF;
	private static final int VALID = 0x95CD41;
	private static final int INVALID = 0xEA5C2B;

	private MovePreview() {
	}

	public static void tick(Minecraft mc) {
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;
		if (player == null || level == null) {
			return;
		}
		Selection selection = Selections.get(player);
		if (selection == null) {
			return;
		}
		BlockPos from = selection.pos();
		if (selection.dimension() != level.dimension()
				|| !(level.getBlockEntity(from) instanceof ChainConveyorBlockEntity)) {
			// Changed dimension, or the server already moved it.
			Selections.clear(player);
			return;
		}
		if (!player.getMainHandItem().is(AllItems.WRENCH) && !player.getOffhandItem().is(AllItems.WRENCH)) {
			return;
		}

		if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
				|| hit.getBlockPos().equals(from)) {
			ring(from, NEUTRAL, "conveyor_move_from");
			player.sendOverlayMessage(Component.translatable("create_conveyor_mover.selected"));
			return;
		}

		BlockPos to = WrenchMove.destination(level, hit.getBlockPos(), hit.getDirection());
		ConveyorMove.Plan plan = ConveyorMove.plan(level, from, to, player);
		int color = plan.valid() ? VALID : INVALID;
		ring(from, color, "conveyor_move_from");
		ring(to, color, "conveyor_move_to");
		int line = 0;
		for (BlockPos neighbour : plan.neighbours()) {
			chain(to, neighbour, color, "conveyor_move_chain" + line++);
		}
		if (plan.chainDelta() > 0 && !player.isCreative()) {
			BlueprintOverlayRenderer.displayChainRequirements(Items.IRON_CHAIN, plan.chainDelta(),
					plan.valid());
		}
		player.sendOverlayMessage(plan.error() != null ? plan.error().copy().withStyle(ChatFormatting.RED)
				: Component.translatable("create_conveyor_mover.valid").withStyle(ChatFormatting.GREEN));
	}

	/** The two octagons Create draws around a conveyor while connecting chains. */
	private static void ring(BlockPos pos, int color, String key) {
		for (int y : Iterate.zeroAndOne) {
			Vec3 previous = VecHelper.rotate(new Vec3(0, .125 + y * .75, 1.25), -22.5, Axis.Y)
					.add(Vec3.atBottomCenterOf(pos));
			for (int i = 0; i < 8; i++) {
				Vec3 next = VecHelper.rotate(new Vec3(0, .125 + y * .75, 1.25), 22.5 + i * 45, Axis.Y)
						.add(Vec3.atBottomCenterOf(pos));
				Outliner.getInstance().showLine(key + y + i, previous, next).lineWidth(1 / 16f).colored(color);
				previous = next;
			}
		}
	}

	private static void chain(BlockPos a, BlockPos b, int color, String key) {
		Vec3 from = Vec3.atCenterOf(a);
		Vec3 to = Vec3.atCenterOf(b);
		Vec3 diff = to.subtract(from);
		if (diff.length() < 1) {
			return;
		}
		from = from.add(diff.normalize().scale(.5));
		to = to.subtract(diff.normalize().scale(.5));
		Vec3 normal = diff.cross(new Vec3(0, 1, 0)).normalize().scale(.875);
		Outliner.getInstance().showLine(key + "a", from.add(normal), to.add(normal)).lineWidth(1 / 16f).colored(color);
		Outliner.getInstance().showLine(key + "b", from.subtract(normal), to.subtract(normal)).lineWidth(1 / 16f)
				.colored(color);
	}
}
