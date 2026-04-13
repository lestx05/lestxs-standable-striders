package com.lestxs.standable.striders.mixin;

import com.lestxs.standable.striders.PlatformStateAccess;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobPlatformFreezeMixin {
	@Shadow
	protected GoalSelector goalSelector;

	@Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
	private void lestxs$cancelServerAiStepWhilePlatform(CallbackInfo ci) {
		if (((Object)this) instanceof PlatformStateAccess access && access.lestxs$isPlatformStill()) {
			ci.cancel();
		}
	}

	@Inject(method = "updateControlFlags", at = @At("TAIL"))
	private void lestxs$keepControlFlagsDisabledWhilePlatform(CallbackInfo ci) {
		if (((Object)this) instanceof PlatformStateAccess access && access.lestxs$isPlatformStill()) {
			this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
			this.goalSelector.disableControlFlag(Goal.Flag.JUMP);
			this.goalSelector.disableControlFlag(Goal.Flag.LOOK);
		}
	}
}
