package io.github.mrshortman.gaugehighlight;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Runs inside Fabric Loader with Create Fly on the classpath. Loading the target class applies our mixin;
 * if Create or Minecraft renamed something it fails to apply and so does this test.
 */
class MixinApplyTest {
	@Test
	void mixinApplies() throws ClassNotFoundException {
		Class<?> handler = Class.forName(
				"com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelConnectionHandler", false,
				getClass().getClassLoader());
		assertTrue(Arrays.stream(handler.getDeclaredMethods())
				.anyMatch(method -> method.getName().contains("gaugeHighlight$highlight")));
	}

	@Test
	void wallIsOnePlaneOfGaugesFacingTheSameWay() {
		BlockPos origin = new BlockPos(10, 64, 10);
		// A wall of gauges facing north spreads along x and y.
		assertTrue(GaugeHighlight.sameWall(origin, Direction.NORTH, new BlockPos(-5, 70, 10), Direction.NORTH));
		assertFalse(GaugeHighlight.sameWall(origin, Direction.NORTH, new BlockPos(10, 64, 11), Direction.NORTH));
		assertFalse(GaugeHighlight.sameWall(origin, Direction.NORTH, new BlockPos(12, 64, 10), Direction.SOUTH));
		assertFalse(GaugeHighlight.sameWall(origin, Direction.NORTH,
				new BlockPos(10 + GaugeHighlight.RANGE + 1, 64, 10), Direction.NORTH));
		// Gauges on a floor spread along x and z.
		assertTrue(GaugeHighlight.sameWall(origin, Direction.UP, new BlockPos(20, 64, -3), Direction.UP));
		assertFalse(GaugeHighlight.sameWall(origin, Direction.UP, new BlockPos(20, 65, -3), Direction.UP));
	}
}
