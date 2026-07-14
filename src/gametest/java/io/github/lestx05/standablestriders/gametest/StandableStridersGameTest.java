package io.github.lestx05.standablestriders.gametest;

import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class StandableStridersGameTest implements CustomTestMethodInvoker {
	private static final double POSITION_TOLERANCE = 1.0E-4;
	private static final float ROTATION_TOLERANCE = 1.0E-4F;

	@GameTest
	@SuppressWarnings("removal")
	public void happyGhastPlatformParity(GameTestHelper context) {
		context.setBlock(0, 0, 0, Blocks.STONE);

		Strider strider = (Strider) context.spawnWithNoFreeWill(
				EntityType.STRIDER,
				new Vec3(0.5, 1.0, 0.5)
		);
		strider.setYRot(52.0F);
		strider.setXRot(28.0F);
		strider.setYHeadRot(52.0F);
		strider.setYBodyRot(52.0F);

		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.setNoGravity(true);
		player.snapTo(
				strider.getX(),
				strider.getBoundingBox().maxY + 0.01,
				strider.getZ(),
				0.0F,
				0.0F
		);

		double lockedX = strider.getX();
		double supportedY = strider.getY();
		double lockedZ = strider.getZ();

		context.startSequence()
				.thenIdle(3)
				.thenExecute(() -> {
					context.assertTrue(
							strider.canBeCollidedWith(player),
							"A supported strider with a player on top must be a platform"
					);
					context.assertTrue(
							Math.abs(strider.getYRot() - 90.0F) <= ROTATION_TOLERANCE,
							"Platform entry must snap 52 degrees to 90 degrees"
					);
					context.assertTrue(
							Math.abs(strider.getXRot()) <= ROTATION_TOLERANCE
									&& Math.abs(strider.xRotO) <= ROTATION_TOLERANCE,
							"Platform state must lock the head pitch straight ahead"
					);

					float healthBeforeDamage = strider.getHealth();
					Vec3 movementBeforeDamage = strider.getDeltaMovement();
					boolean tookDamage = strider.hurtServer(
							context.getLevel(),
							strider.damageSources().playerAttack(player),
							2.0F
					);
					context.assertTrue(
							tookDamage && strider.getHealth() < healthBeforeDamage,
							"Platform locking must not make the strider immune to damage"
					);
					context.assertTrue(
							strider.getDeltaMovement().distanceToSqr(movementBeforeDamage)
									<= POSITION_TOLERANCE * POSITION_TOLERANCE,
							"Damage must not apply knockback while platformed"
					);

					strider.knockback(1.0, 1.0, 1.0);
					strider.push(new Vec3(0.6, 0.6, 0.6));
					context.assertTrue(
							strider.getDeltaMovement().distanceToSqr(movementBeforeDamage)
									<= POSITION_TOLERANCE * POSITION_TOLERANCE,
							"Platform state must reject knockback and external push vectors"
					);
					strider.setDeltaMovement(0.4, strider.getDeltaMovement().y, 0.4);
				})
				.thenIdle(2)
				.thenExecute(() -> {
					context.assertTrue(
							Math.abs(strider.getX() - lockedX) <= POSITION_TOLERANCE
									&& Math.abs(strider.getZ() - lockedZ) <= POSITION_TOLERANCE,
							"Platform state must reject horizontal movement"
					);
					context.assertTrue(
							Math.abs(strider.getYRot() - 90.0F) <= ROTATION_TOLERANCE,
							"Platform state must retain its snapped rotation"
					);
					context.assertTrue(
							Math.abs(strider.getXRot()) <= ROTATION_TOLERANCE,
							"Platform state must keep the head pitch locked after damage"
					);
					player.snapTo(
							strider.getX() + 10.0,
							player.getY(),
							strider.getZ() + 10.0,
							0.0F,
							0.0F
					);
				})
				.thenIdle(5)
				.thenExecute(() -> context.assertTrue(
						strider.canBeCollidedWith(player),
						"Platform state must retain the Happy Ghast grace period"
				))
				.thenExecute(() -> context.setBlock(0, 0, 0, Blocks.AIR))
				.thenIdle(2)
				.thenExecute(() -> {
					context.assertFalse(
							strider.canBeCollidedWith(player),
							"Platform state must end immediately when block support disappears"
					);
					context.assertTrue(
							strider.getY() < supportedY,
							"The unsupported strider must resume falling"
					);
					player.discard();
				})
				.thenSucceed();
	}

	@Override
	public void invokeTestMethod(
			GameTestHelper context,
			Method method
	) throws ReflectiveOperationException {
		method.invoke(this, context);
	}
}
