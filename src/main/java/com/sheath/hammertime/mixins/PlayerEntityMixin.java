package com.sheath.hammertime.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"))
    private void modifyBlockBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        // Reset behavior: Do not modify block breaking speed
    }
}
