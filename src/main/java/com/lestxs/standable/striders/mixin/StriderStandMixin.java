package com.lestxs.standable.striders.mixin;

import com.lestxs.standable.striders.PlatformStateAccess;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Strider.class)
public abstract class StriderStandMixin extends Animal implements PlatformStateAccess {
	@Unique
	private static final EntityDataAccessor<Boolean> LESTXS_STAYS_STILL =
			SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);

	@Unique
	private static final double LESTXS_HORIZONTAL_PADDING = 0.28D;

	@Unique
	private static final double LESTXS_TOP_FACE_TOLERANCE = 0.25D;

	@Unique
	private static final double LESTXS_ITEM_TOP_FACE_TOLERANCE = 0.35D;

	@Unique
	private static final double LESTXS_MAX_CONTACT_HEIGHT = 0.42D;

	@Unique
	private static final double LESTXS_ENTRY_TOP_FACE_TOLERANCE = 0.45D;

	@Unique
	private static final double LESTXS_ENTRY_LANDING_BUFFER = 0.08D;

	@Unique
	private static final int LESTXS_ROTATION_SETTLE_TICKS = 3;

	@Unique
	private static final float LESTXS_MAX_ENTRY_YAW_STEP = 30.0F;

	@Unique
	private boolean lestxs$wasStillLastTick = false;

	@Unique
	private float lestxs$lastFreeHeadYaw = 0.0F;

	@Unique
	private float lestxs$frozenYaw = 0.0F;

	@Unique
	private boolean lestxs$lookControlDisabled = false;

	@Unique
	private boolean lestxs$moveControlDisabled = false;

	@Unique
	private int lestxs$rotationSettleTicks = 0;

	protected StriderStandMixin(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void lestxs$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(LESTXS_STAYS_STILL, false);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void lestxs$tick(CallbackInfo ci) {
		if (!this.lestxs$canUseStandablePlatform()) {
			if (!this.level().isClientSide()) {
				this.lestxs$setStill(false);
				this.lestxs$setLookAroundEnabled(true);
				this.lestxs$setMovementAiEnabled(true);
			}

			this.lestxs$wasStillLastTick = false;
			this.setRequiresPrecisePosition(false);
			return;
		}

		// Antes esto contribuía al snap/ajuste fuerte encima del strider.
		// Lo dejamos siempre en false para que el movimiento se sienta mucho más natural.
		this.setRequiresPrecisePosition(false);

		boolean hasPlatformUser = false;
		if (!this.level().isClientSide()) {
			AABB surfaceScanBox = this.lestxs$getSurfaceScanBox();
			hasPlatformUser = this.lestxs$hasPlatformUser(surfaceScanBox);
			this.lestxs$setStill(hasPlatformUser);
		}

		boolean staysStill = this.level().isClientSide() ? this.lestxs$isStill() : hasPlatformUser;
		boolean justEnteredStill = staysStill && !this.lestxs$wasStillLastTick;

		if (!this.level().isClientSide()) {
			this.lestxs$setLookAroundEnabled(!staysStill);
			this.lestxs$setMovementAiEnabled(!staysStill);
		}

		if (staysStill) {
			if (justEnteredStill) {
				this.lestxs$frozenYaw = lestxs$snapToRightAngle(this.lestxs$lastFreeHeadYaw);
				this.lestxs$rotationSettleTicks = LESTXS_ROTATION_SETTLE_TICKS;
			}

			if (!this.level().isClientSide()) {
				this.lestxs$holdPlatformMotion(justEnteredStill);
			}

			this.lestxs$applyFrozenPlatformRotation();
		} else {
			this.lestxs$lastFreeHeadYaw = this.yHeadRot;
		}

		this.lestxs$wasStillLastTick = staysStill;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void lestxs$preTickFreezePlatform(CallbackInfo ci) {
		if (this.lestxs$isStill()) {
			this.lestxs$freezeHorizontalMotion();
		}
	}

	@Inject(method = "getDismountLocationForPassenger", at = @At("HEAD"), cancellable = true)
	private void lestxs$putPassengerOnTopWhenDismounting(LivingEntity passenger, CallbackInfoReturnable<Vec3> cir) {
		if (!(passenger instanceof Player) || !this.lestxs$canUseStandablePlatform()) {
			return;
		}

		passenger.setPose(Pose.STANDING);
		cir.setReturnValue(this.lestxs$getGuaranteedTopDismountLocation());
	}

	@Override
	public boolean canBeCollidedWith(Entity entity) {
		return this.lestxs$canActAsPlatformFor(entity);
	}

	@Override
	public boolean isPushable() {
		return !this.lestxs$isStill() && super.isPushable();
	}

	@Override
	public boolean isPushedByFluid() {
		return !this.lestxs$isStill() && super.isPushedByFluid();
	}

	@Unique
	private void lestxs$holdPlatformMotion(boolean justEnteredStill) {
		this.lestxs$freezeHorizontalMotion();
	}

	@Unique
	private void lestxs$applyFrozenPlatformRotation() {
		float targetYaw = this.lestxs$frozenYaw;
		float appliedYaw = targetYaw;

		if (this.lestxs$rotationSettleTicks > 0) {
			float currentYaw = this.getYRot();
			float deltaYaw = lestxs$wrapDegrees(targetYaw - currentYaw);
			float clampedStep = Mth.clamp(deltaYaw, -LESTXS_MAX_ENTRY_YAW_STEP, LESTXS_MAX_ENTRY_YAW_STEP);

			appliedYaw = currentYaw + clampedStep;
			this.lestxs$rotationSettleTicks--;
		}
		this.setYRot(appliedYaw);
		this.yRotO = appliedYaw;

		this.yBodyRot = appliedYaw;
		this.yBodyRotO = appliedYaw;

		this.yHeadRot = appliedYaw;
		this.yHeadRotO = appliedYaw;

		this.setXRot(0.0F);
		this.xRotO = 0.0F;
	}

	@Unique
	private void lestxs$setStill(boolean staysStill) {
		this.getEntityData().set(LESTXS_STAYS_STILL, staysStill);
	}

	@Unique
	private void lestxs$freezeHorizontalMotion() {
		this.getNavigation().stop();
		this.getMoveControl().setWait();
		this.setTarget(null);
		this.setXxa(0.0F);
		this.setYya(0.0F);
		this.setZza(0.0F);
		this.setSpeed(0.0F);
		this.jumping = false;

		Vec3 motion = this.getDeltaMovement();
		this.setDeltaMovement(0.0D, motion.y, 0.0D);
	}

	@Unique
	private void lestxs$setLookAroundEnabled(boolean enabled) {
		if (enabled == !this.lestxs$lookControlDisabled) {
			return;
		}

		if (enabled) {
			this.goalSelector.enableControlFlag(Goal.Flag.LOOK);
		} else {
			this.goalSelector.disableControlFlag(Goal.Flag.LOOK);
		}

		this.lestxs$lookControlDisabled = !enabled;
	}

	@Unique
	private void lestxs$setMovementAiEnabled(boolean enabled) {
		if (enabled == !this.lestxs$moveControlDisabled) {
			return;
		}

		if (enabled) {
			this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
			this.goalSelector.enableControlFlag(Goal.Flag.JUMP);
		} else {
			this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
			this.goalSelector.disableControlFlag(Goal.Flag.JUMP);
		}

		this.lestxs$moveControlDisabled = !enabled;
	}

	@Unique
	private boolean lestxs$isStill() {
		return this.getEntityData().get(LESTXS_STAYS_STILL);
	}

	@Override
	public boolean lestxs$isPlatformStill() {
		return this.lestxs$isStill();
	}

	@Unique
	private boolean lestxs$hasPlatformUser(AABB surfaceScanBox) {
		return !this.level().getEntitiesOfClass(Player.class, surfaceScanBox, player -> this.lestxs$isStandingPlatformPassenger(player, surfaceScanBox)).isEmpty();
	}

	@Unique
	private boolean lestxs$isStandingPlatformPassenger(Player player, AABB surfaceScanBox) {
		return this.lestxs$isEligiblePlatformPassenger(player)
				&& this.lestxs$hasTopFaceContact(player, surfaceScanBox, LESTXS_TOP_FACE_TOLERANCE, LESTXS_MAX_CONTACT_HEIGHT);
	}

	@Unique
	private boolean lestxs$isEligiblePlatformPassenger(Player player) {
		if (!player.isAlive() || player.isSpectator()) {
			return false;
		}

		// Igual que la idea original: ignorar si ya va montado en algo.
		return player.getVehicle() == null;
	}

	private boolean lestxs$canActAsPlatformFor(Entity entity) {
		if (entity == null || entity == this) {
			return false;
		}

		if (!this.isAlive() || !this.lestxs$canUseStandablePlatform()) {
			return false;
		}

		if (entity.getVehicle() == this) {
			return false;
		}

		AABB surfaceBox = this.lestxs$getSurfaceScanBox();
		double tolerance = entity instanceof ItemEntity
				? LESTXS_ITEM_TOP_FACE_TOLERANCE
				: LESTXS_TOP_FACE_TOLERANCE;

		if (entity instanceof Player player && this.lestxs$isEligiblePlatformPassenger(player)) {
			if (this.lestxs$isStandingPlatformPassenger(player, surfaceBox)) {
				return true;
			}

			if (this.lestxs$isLandingOnPlatform(player, surfaceBox)) {
				return true;
			}

			return false;
		}

		if (!this.lestxs$isStill()) {
			return false;
		}

		return this.lestxs$hasTopFaceContact(entity, surfaceBox, tolerance, LESTXS_MAX_CONTACT_HEIGHT);
	}

	@Unique
	private boolean lestxs$isLandingOnPlatform(Player player, AABB surfaceBox) {
		Vec3 velocity = player.getDeltaMovement();
		if (velocity.y > 0.0D) {
			return false;
		}

		AABB entityBox = player.getBoundingBox();
		if (!this.lestxs$hasHorizontalTopOverlap(entityBox, surfaceBox)) {
			return false;
		}

		double striderTopY = this.getBoundingBox().maxY;
		double currentFeetY = entityBox.minY;
		double previousFeetY = currentFeetY - velocity.y;
		double highestFeetY = Math.max(previousFeetY, currentFeetY);
		double lowestFeetY = Math.min(previousFeetY, currentFeetY);

		return highestFeetY >= striderTopY - LESTXS_ENTRY_TOP_FACE_TOLERANCE
				&& lowestFeetY <= striderTopY + LESTXS_ENTRY_LANDING_BUFFER;
	}

	@Unique
	private boolean lestxs$hasTopFaceContact(Entity entity, AABB surfaceBox, double topTolerance, double maxContactHeight) {
		AABB entityBox = entity.getBoundingBox();
		double entityFeetY = entityBox.minY;
		double striderTopY = this.getBoundingBox().maxY;

		if (entityFeetY < striderTopY - topTolerance) {
			return false;
		}

		if (entityFeetY > striderTopY + maxContactHeight) {
			return false;
		}

		return this.lestxs$hasHorizontalTopOverlap(entityBox, surfaceBox);
	}

	@Unique
	private boolean lestxs$hasHorizontalTopOverlap(AABB entityBox, AABB surfaceBox) {
		return entityBox.maxX > surfaceBox.minX
				&& entityBox.minX < surfaceBox.maxX
				&& entityBox.maxZ > surfaceBox.minZ
				&& entityBox.minZ < surfaceBox.maxZ;
	}

	@Unique
	private AABB lestxs$getSurfaceScanBox() {
		AABB box = this.getBoundingBox();

		return new AABB(
				box.minX - LESTXS_HORIZONTAL_PADDING,
				box.maxY - LESTXS_ITEM_TOP_FACE_TOLERANCE,
				box.minZ - LESTXS_HORIZONTAL_PADDING,
				box.maxX + LESTXS_HORIZONTAL_PADDING,
				box.maxY + LESTXS_MAX_CONTACT_HEIGHT,
				box.maxZ + LESTXS_HORIZONTAL_PADDING
		);
	}

	@Unique
	private Vec3 lestxs$getGuaranteedTopDismountLocation() {
		return new Vec3(
				this.getX(),
				this.getBoundingBox().maxY + 0.1D,
				this.getZ()
		);
	}

	@Unique
	private boolean lestxs$canUseStandablePlatform() {
		return !this.isBaby();
	}

	@Unique
	private static float lestxs$snapToRightAngle(float yaw) {
		return Math.round(yaw / 90.0F) * 90.0F;
	}

	@Unique
	private static float lestxs$wrapDegrees(float degrees) {
		float wrapped = degrees % 360.0F;

		if (wrapped >= 180.0F) {
			wrapped -= 360.0F;
		}

		if (wrapped < -180.0F) {
			wrapped += 360.0F;
		}

		return wrapped;
	}

}
