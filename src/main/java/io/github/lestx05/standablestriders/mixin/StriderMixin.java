package io.github.lestx05.standablestriders.mixin;

import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Strider.class)
public abstract class StriderMixin extends Animal {
	@Unique
	private static final int STANDABLE_STRIDERS$STILL_TIMEOUT_ON_LOAD_GRACE_PERIOD = 60;

	@Unique
	private static final int STANDABLE_STRIDERS$MAX_STILL_TIMEOUT = 10;

	@Unique
	private static final double STANDABLE_STRIDERS$SUPPORT_DEPTH = 0.0625;

	@Unique
	private static final double STANDABLE_STRIDERS$COLLISION_EPSILON = 1.0E-5;

	@Unique
	private static final String STANDABLE_STRIDERS$STILL_TIMEOUT_TAG =
			"standable_striders.still_timeout";

	@Unique
	private static final EntityDataAccessor<Boolean> STANDABLE_STRIDERS$STAYS_STILL =
			SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);

	@Unique
	private int standableStriders$serverStillTimeout;

	@Unique
	private boolean standableStriders$platformLockActive;

	@Unique
	private double standableStriders$lockedX;

	@Unique
	private double standableStriders$lockedZ;

	@Unique
	private float standableStriders$lockedYaw;

	protected StriderMixin(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void standableStriders$definePlatformState(
			SynchedEntityData.Builder entityData,
			CallbackInfo callbackInfo
	) {
		entityData.define(STANDABLE_STRIDERS$STAYS_STILL, false);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void standableStriders$beforeTick(CallbackInfo callbackInfo) {
		Strider strider = standableStriders$self();

		if (!level().isClientSide()
				&& standableStriders$serverStillTimeout > 0
				&& !standableStriders$canUsePlatformState(strider)) {
			standableStriders$setServerStillTimeout(0);
		}

		boolean platformActive = standableStriders$isPlatformActive(strider);
		if (!level().isClientSide()) {
			setRequiresPrecisePosition(platformActive);
		}
		if (platformActive) {
			standableStriders$beginPlatformLockIfNeeded(strider);
			standableStriders$applyPlatformLock(strider);
		} else {
			standableStriders$platformLockActive = false;
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void standableStriders$afterTick(CallbackInfo callbackInfo) {
		Strider strider = standableStriders$self();

		if (!level().isClientSide()) {
			if (!standableStriders$canUsePlatformState(strider)) {
				standableStriders$setServerStillTimeout(0);
			} else {
				if (standableStriders$serverStillTimeout > 0) {
					if (tickCount > STANDABLE_STRIDERS$STILL_TIMEOUT_ON_LOAD_GRACE_PERIOD) {
						standableStriders$serverStillTimeout--;
					}

					standableStriders$setServerStillTimeout(standableStriders$serverStillTimeout);
				}

				if (standableStriders$hasPlayerOnTop(strider)) {
					standableStriders$setServerStillTimeout(STANDABLE_STRIDERS$MAX_STILL_TIMEOUT);
				}
			}
		}

		boolean platformActive = standableStriders$isPlatformActive(strider);
		if (!level().isClientSide()) {
			setRequiresPrecisePosition(platformActive);
		}
		if (platformActive) {
			standableStriders$beginPlatformLockIfNeeded(strider);
			standableStriders$applyPlatformLock(strider);
		} else {
			standableStriders$platformLockActive = false;
		}
	}

	@Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
	private void standableStriders$disableRiderControlWhilePlatformed(
			CallbackInfoReturnable<LivingEntity> callbackInfo
	) {
		if (standableStriders$isPlatformActive(standableStriders$self())) {
			callbackInfo.setReturnValue(null);
		}
	}

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);
		if (level().isClientSide()) {
			return;
		}

		Strider strider = standableStriders$self();
		if (!standableStriders$canUsePlatformState(strider)
				|| !standableStriders$hasPlayerOnTop(strider)) {
			standableStriders$setServerStillTimeout(0);
		} else if (standableStriders$serverStillTimeout > STANDABLE_STRIDERS$MAX_STILL_TIMEOUT) {
			standableStriders$setServerStillTimeout(STANDABLE_STRIDERS$MAX_STILL_TIMEOUT);
		}
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		if (level().isClientSide()) {
			return;
		}

		Strider strider = standableStriders$self();
		standableStriders$setServerStillTimeout(
				standableStriders$canUsePlatformState(strider)
						? STANDABLE_STRIDERS$MAX_STILL_TIMEOUT
						: 0
		);
	}

	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt(
				STANDABLE_STRIDERS$STILL_TIMEOUT_TAG,
				standableStriders$serverStillTimeout
		);
	}

	@Override
	public void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		standableStriders$setServerStillTimeout(
				input.getIntOr(STANDABLE_STRIDERS$STILL_TIMEOUT_TAG, 0)
		);
	}

	/**
	 * Mirrors the adult Happy Ghast's platform collision rules, with one
	 * intentional Strider-specific condition: a collidable block must directly
	 * support the Strider.
	 */
	@Override
	public boolean canBeCollidedWith(@Nullable Entity other) {
		Strider strider = standableStriders$self();
		if (!standableStriders$canUsePlatformState(strider)) {
			return false;
		}

		if (level().isClientSide()
				&& other instanceof Player
				&& other.position().y >= strider.getBoundingBox().maxY) {
			return true;
		}

		return standableStriders$isOnStillTimeout();
	}

	@Unique
	private void standableStriders$setServerStillTimeout(int stillTimeout) {
		if (standableStriders$serverStillTimeout <= 0 && stillTimeout > 0) {
			Strider strider = standableStriders$self();
			standableStriders$beginPlatformLockIfNeeded(strider);

			if (level() instanceof ServerLevel serverLevel) {
				syncPacketPositionCodec(getX(), getY(), getZ());
				serverLevel.getChunkSource().chunkMap.sendToTrackingPlayers(
						strider,
						ClientboundEntityPositionSyncPacket.of(strider)
				);
			}
		}

		standableStriders$serverStillTimeout = stillTimeout;
		entityData.set(STANDABLE_STRIDERS$STAYS_STILL, stillTimeout > 0);

		if (stillTimeout <= 0) {
			standableStriders$platformLockActive = false;
		}
	}

	@Unique
	private boolean standableStriders$isOnStillTimeout() {
		return entityData.get(STANDABLE_STRIDERS$STAYS_STILL)
				|| standableStriders$serverStillTimeout > 0;
	}

	@Unique
	private boolean standableStriders$isPlatformActive(Strider strider) {
		return standableStriders$canUsePlatformState(strider)
				&& standableStriders$isOnStillTimeout();
	}

	@Unique
	private boolean standableStriders$canUsePlatformState(Strider strider) {
		return !strider.isBaby()
				&& strider.isAlive()
				&& standableStriders$hasSupportingBlock(strider);
	}

	@Unique
	private boolean standableStriders$hasSupportingBlock(Strider strider) {
		AABB bounds = strider.getBoundingBox();
		AABB supportProbe = new AABB(
				bounds.minX + STANDABLE_STRIDERS$COLLISION_EPSILON,
				bounds.minY - STANDABLE_STRIDERS$SUPPORT_DEPTH,
				bounds.minZ + STANDABLE_STRIDERS$COLLISION_EPSILON,
				bounds.maxX - STANDABLE_STRIDERS$COLLISION_EPSILON,
				bounds.minY + STANDABLE_STRIDERS$COLLISION_EPSILON,
				bounds.maxZ - STANDABLE_STRIDERS$COLLISION_EPSILON
		);

		return !level().noBlockCollision(strider, supportProbe);
	}

	@Unique
	private boolean standableStriders$hasPlayerOnTop(Strider strider) {
		AABB bounds = strider.getBoundingBox();
		AABB detectionArea = new AABB(
				bounds.minX - 1.0,
				bounds.maxY - 1.0E-5F,
				bounds.minZ - 1.0,
				bounds.maxX + 1.0,
				bounds.maxY + bounds.getYsize() / 2.0,
				bounds.maxZ + 1.0
		);

		for (Player player : level().players()) {
			if (player.isSpectator()) {
				continue;
			}

			Entity rootVehicle = player.getRootVehicle();
			if (!(rootVehicle instanceof Strider)
					&& detectionArea.contains(rootVehicle.position())) {
				return true;
			}
		}

		return false;
	}

	@Unique
	private void standableStriders$beginPlatformLockIfNeeded(Strider strider) {
		if (standableStriders$platformLockActive) {
			return;
		}

		standableStriders$platformLockActive = true;
		standableStriders$lockedX = strider.getX();
		standableStriders$lockedZ = strider.getZ();
		standableStriders$lockedYaw = strider.getYRot()
				- standableStriders$wrapDegrees90(strider.getYRot());
		standableStriders$applyPlatformRotation(strider);
	}

	@Unique
	private void standableStriders$applyPlatformLock(Strider strider) {
		strider.setPos(
				standableStriders$lockedX,
				strider.getY(),
				standableStriders$lockedZ
		);
		standableStriders$stopHorizontalMovement(strider);
		standableStriders$applyPlatformRotation(strider);
	}

	@Unique
	private void standableStriders$stopHorizontalMovement(Strider strider) {
		strider.getNavigation().stop();
		strider.setXxa(0.0F);
		strider.setYya(0.0F);
		strider.setSpeed(0.0F);

		Vec3 movement = strider.getDeltaMovement();
		strider.setDeltaMovement(0.0, movement.y, 0.0);
	}

	@Unique
	private void standableStriders$applyPlatformRotation(Strider strider) {
		strider.setYRot(standableStriders$lockedYaw);
		strider.setYHeadRot(standableStriders$lockedYaw);
		strider.yBodyRot = standableStriders$lockedYaw;
		strider.yHeadRot = standableStriders$lockedYaw;
	}

	@Unique
	private static float standableStriders$wrapDegrees90(float angle) {
		float normalizedAngle = angle % 90.0F;
		if (normalizedAngle >= 45.0F) {
			normalizedAngle -= 90.0F;
		}

		if (normalizedAngle < -45.0F) {
			normalizedAngle += 90.0F;
		}

		return normalizedAngle;
	}

	@Unique
	private Strider standableStriders$self() {
		return (Strider) (Object) this;
	}
}
