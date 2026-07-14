package io.github.lestx05.standablestriders.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Strider.class)
public abstract class StriderMixin extends Entity {
	@Unique
	private static final int STANDABLE_STRIDERS$STILL_TIMEOUT = 10;

	@Unique
	private int standableStriders$stillTicks;

	@Unique
	private double standableStriders$lockedX;

	@Unique
	private double standableStriders$lockedY;

	@Unique
	private double standableStriders$lockedZ;

	protected StriderMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void standableStriders$beginTick(CallbackInfo callbackInfo) {
		Strider strider = standableStriders$self();

		if (standableStriders$stillTicks > 0) {
			standableStriders$stillTicks--;
		}

		if (standableStriders$hasPlayerOnTop(strider)) {
			standableStriders$stillTicks = STANDABLE_STRIDERS$STILL_TIMEOUT;
		}

		if (standableStriders$isStayingStill()) {
			standableStriders$lockedX = strider.getX();
			standableStriders$lockedY = strider.getY();
			standableStriders$lockedZ = strider.getZ();
			standableStriders$stopMovement(strider);
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void standableStriders$endTick(CallbackInfo callbackInfo) {
		if (!standableStriders$isStayingStill()) {
			return;
		}

		Strider strider = standableStriders$self();
		strider.setPos(
				standableStriders$lockedX,
				standableStriders$lockedY,
				standableStriders$lockedZ
		);
		standableStriders$stopMovement(strider);
	}

	/**
	 * Mirrors the Happy Ghast's platform collision rules: an alive strider becomes
	 * collidable while its short stillness timeout is active. On the client, a
	 * player already above the top face is accepted immediately to avoid falling
	 * through before the server state catches up.
	 */
	@Override
	public boolean canBeCollidedWith(@Nullable Entity entity) {
		Strider strider = standableStriders$self();
		if (!strider.isAlive()) {
			return false;
		}

		if (strider.level().isClientSide()
				&& entity instanceof Player
				&& entity.getBoundingBox().minY >= strider.getBoundingBox().maxY) {
			return true;
		}

		return standableStriders$isStayingStill();
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

		for (Player player : strider.level().players()) {
			if (player.isSpectator()) {
				continue;
			}

			Entity rootVehicle = player.getRootVehicle();
			if (rootVehicle instanceof Strider) {
				continue;
			}

			if (detectionArea.intersects(rootVehicle.getBoundingBox())) {
				return true;
			}
		}

		return false;
	}

	@Unique
	private boolean standableStriders$isStayingStill() {
		return standableStriders$stillTicks > 0;
	}

	@Unique
	private void standableStriders$stopMovement(Strider strider) {
		strider.getNavigation().stop();
		strider.setSpeed(0.0F);
		strider.setDeltaMovement(Vec3.ZERO);
	}

	@Unique
	private Strider standableStriders$self() {
		return (Strider) (Object) this;
	}
}
