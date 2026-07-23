package io.github.lestx05.standablestriders.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Method;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("removal")
public final class StandableStridersGameTest implements CustomTestMethodInvoker {
	private static final double POSITION_TOLERANCE = 1.0E-4;
	private static final float ROTATION_TOLERANCE = 1.0E-4F;

	@GameTest
	public void happyGhastPlatformParity(GameTestHelper context) {
		context.setBlock(0, 0, 0, Blocks.STONE);

		Strider strider = (Strider) context.spawnWithNoFreeWill(
				EntityTypes.STRIDER,
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
							Math.abs(strider.yRotO - 90.0F) <= ROTATION_TOLERANCE
									&& Math.abs(strider.getYHeadRot() - 90.0F)
											<= ROTATION_TOLERANCE
									&& Math.abs(strider.yHeadRotO - 90.0F)
											<= ROTATION_TOLERANCE
									&& Math.abs(strider.yBodyRot - 90.0F)
											<= ROTATION_TOLERANCE
									&& Math.abs(strider.yBodyRotO - 90.0F)
											<= ROTATION_TOLERANCE,
							"Platform state must lock body, head, and previous yaw values"
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

					strider.knockback(
							1.0,
							1.0,
							1.0,
							strider.damageSources().playerAttack(player),
							1.0F,
							false
					);
					strider.push(new Vec3(0.6, 0.6, 0.6));
					context.assertTrue(
							strider.getDeltaMovement().distanceToSqr(movementBeforeDamage)
									<= POSITION_TOLERANCE * POSITION_TOLERANCE,
							"Platform state must reject knockback and external push vectors"
					);
					strider.setLeashedTo(player, false);
					strider.getLeashData().angularMomentum = 1.0;
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
					context.assertTrue(
							Math.abs(strider.getLeashData().angularMomentum)
									<= POSITION_TOLERANCE,
							"Platform state must clear angular leash momentum"
					);
					strider.removeLeash();
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

	@GameTest(maxTicks = 100)
	public void platformTimeoutExpiresAfterTenTicks(GameTestHelper context) {
		context.setBlock(0, 0, 0, Blocks.STONE);

		Strider strider = (Strider) context.spawnWithNoFreeWill(
				EntityTypes.STRIDER,
				new Vec3(0.5, 1.0, 0.5)
		);
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.setNoGravity(true);
		player.snapTo(
				strider.getX(),
				strider.getBoundingBox().maxY + 0.01,
				strider.getZ(),
				0.0F,
				0.0F
		);

		context.startSequence()
				.thenIdle(65)
				.thenExecute(() -> player.snapTo(
						strider.getX() + 10.0,
						player.getY(),
						strider.getZ() + 10.0,
						0.0F,
						0.0F
				))
				.thenIdle(9)
				.thenExecute(() -> context.assertTrue(
						strider.canBeCollidedWith(player),
						"Platform state must remain active through timeout tick nine"
				))
				.thenIdle(1)
				.thenExecute(() -> {
					context.assertFalse(
							strider.canBeCollidedWith(player),
							"Platform state must expire after ten ticks without a player"
					);
					player.discard();
				})
				.thenSucceed();
	}

	@GameTest
	public void unsupportedLavaNeverActivatesPlatform(GameTestHelper context) {
		context.setBlock(0, 0, 0, Blocks.LAVA);

		Strider strider = (Strider) context.spawnWithNoFreeWill(
				EntityTypes.STRIDER,
				new Vec3(0.5, 1.0, 0.5)
		);
		strider.setNoGravity(true);
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.setNoGravity(true);
		player.snapTo(
				strider.getX(),
				strider.getBoundingBox().maxY + 0.01,
				strider.getZ(),
				0.0F,
				0.0F
		);

		context.startSequence()
				.thenIdle(3)
				.thenExecute(() -> {
					context.assertFalse(
							strider.canBeCollidedWith(player),
							"Lava without a collidable block must not support platform mode"
					);
					player.discard();
				})
				.thenSucceed();
	}

	@GameTest
	public void babiesNeverActivatePlatform(GameTestHelper context) {
		context.setBlock(0, 0, 0, Blocks.STONE);

		Strider strider = (Strider) context.spawnWithNoFreeWill(
				EntityTypes.STRIDER,
				new Vec3(0.5, 1.0, 0.5)
		);
		strider.setBaby(true);
		ServerPlayer player = context.makeMockServerPlayerInLevel();
		player.setNoGravity(true);
		player.snapTo(
				strider.getX(),
				strider.getBoundingBox().maxY + 0.01,
				strider.getZ(),
				0.0F,
				0.0F
		);

		context.startSequence()
				.thenIdle(3)
				.thenExecute(() -> {
					context.assertFalse(
							strider.canBeCollidedWith(player),
							"Baby striders must never become platforms"
					);
					player.discard();
				})
				.thenSucceed();
	}

	@GameTest
	public void spectatorsAndRidersAreIgnored(GameTestHelper context) {
		context.setBlock(0, 0, 0, Blocks.STONE);

		Strider strider = (Strider) context.spawnWithNoFreeWill(
				EntityTypes.STRIDER,
				new Vec3(0.5, 1.0, 0.5)
		);
		Strider otherStrider = (Strider) context.spawnWithNoFreeWill(
				EntityTypes.STRIDER,
				new Vec3(3.5, 1.0, 0.5)
		);
		ServerPlayer player = makeServerPlayerInLevel(context, "spectator-test");
		player.setNoGravity(true);
		context.assertTrue(
				player.setGameMode(GameType.SPECTATOR) && player.isSpectator(),
				"Test player must enter spectator mode"
		);
		player.snapTo(
				strider.getX(),
				strider.getBoundingBox().maxY + 0.01,
				strider.getZ(),
				0.0F,
				0.0F
		);

		context.startSequence()
				.thenIdle(3)
				.thenExecute(() -> {
					context.assertFalse(
							strider.canBeCollidedWith(player),
							"Spectators above a strider must not activate platform mode"
					);
					player.setGameMode(GameType.SURVIVAL);
					context.assertTrue(
							player.startRiding(strider, true, false),
							"Mock player must be able to ride the strider for this test"
					);
				})
				.thenIdle(3)
				.thenExecute(() -> {
					context.assertFalse(
							strider.canBeCollidedWith(player),
							"A player riding the same strider must not activate platform mode"
					);
					context.assertTrue(
							strider.canBeCollidedWith(otherStrider),
							"A ridden strider must retain the Happy Ghast vehicle collision rule"
					);
					player.stopRiding();
					context.getLevel().getServer().getPlayerList().remove(player);
				})
				.thenSucceed();
	}

	private static ServerPlayer makeServerPlayerInLevel(
			GameTestHelper context,
			String name
	) {
		GameProfile profile = new GameProfile(UUID.randomUUID(), name);
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
		ServerPlayer player = new ServerPlayer(
				context.getLevel().getServer(),
				context.getLevel(),
				profile,
				cookie.clientInformation()
		);
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		context.getLevel().getServer().getPlayerList().placeNewPlayer(
				connection,
				player,
				cookie
		);
		return player;
	}

	@Override
	public void invokeTestMethod(
			GameTestHelper context,
			Method method
	) throws ReflectiveOperationException {
		method.invoke(this, context);
	}
}
