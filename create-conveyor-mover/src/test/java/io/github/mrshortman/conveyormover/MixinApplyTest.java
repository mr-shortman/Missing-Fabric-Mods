package io.github.mrshortman.conveyormover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

/**
 * Runs inside Fabric Loader with Create Fly on the classpath. Loading the target classes applies our mixins;
 * if Create or Minecraft renamed something they fail to apply and so does this test.
 */
class MixinApplyTest {
	@Test
	void mixinsApply() throws ClassNotFoundException {
		assertTrue(hasMethod(load("com.zurrtum.create.content.equipment.wrench.WrenchItem"),
				"conveyorMover$moveConveyor"));
		assertTrue(hasMethod(
				load("com.zurrtum.create.client.content.kinetics.chainConveyor.ChainConveyorConnectionHandler"),
				"conveyorMover$preview"));
	}

	@Test
	void connectionRulesMatchCreate() {
		BlockPos origin = BlockPos.ZERO;
		assertNull(ConveyorMove.connectionProblem(origin, new BlockPos(8, 2, 0), 32));
		assertEquals("create.chain_conveyor.too_far", ConveyorMove.connectionProblem(origin, new BlockPos(40, 0, 0), 32));
		assertEquals("create.chain_conveyor.too_close", ConveyorMove.connectionProblem(origin, new BlockPos(2, 0, 0), 32));
		assertEquals("create.chain_conveyor.cannot_connect_vertically",
				ConveyorMove.connectionProblem(origin, new BlockPos(1, 5, 0), 32));
		assertEquals("create.chain_conveyor.too_steep", ConveyorMove.connectionProblem(origin, new BlockPos(5, 6, 0), 32));
	}

	private Class<?> load(String name) throws ClassNotFoundException {
		return Class.forName(name, false, getClass().getClassLoader());
	}

	private static boolean hasMethod(Class<?> type, String namePart) {
		return Arrays.stream(type.getDeclaredMethods()).anyMatch(method -> method.getName().contains(namePart));
	}
}
