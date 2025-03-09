package com.sheath.hammertime.mixins;

import com.sheath.hammertime.events.HammerHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
    private void modifyBlockBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Get the original mining speed without re-calling getBlockBreakingSpeed()
        float originalSpeed = player.getMainHandStack().getMiningSpeedMultiplier(state);

        // If originalSpeed is zero, keep the default behavior
        if (originalSpeed <= 0) {
            return;
        }

        // Apply slowdown factor
        float slowdownFactor = HammerHandler.applySlowdown((ServerPlayerEntity) player, state);
        float newSpeed = originalSpeed / slowdownFactor;

        // Ensure we never return zero (prevents infinite mining)
        cir.setReturnValue(Math.max(0.01f, newSpeed));
    }
}


