package com.sheath.hammermining.events.hammer;

import com.sheath.hammermining.HammerMining;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class HammerSlowdown {

    public static float getMaxHardnessIn3x3(World world, BlockPos pos, ItemStack tool, PlayerEntity player) {
        float maxHardness = 0.0f;

        Direction facing = player.getHorizontalFacing(); // Get the player's facing direction
        boolean isVertical = player.getPitch(1.0F) < -45 || player.getPitch(1.0F) > 45; // Looking up or down

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos targetPos;

                    if (isVertical) {
                        // **Looking up/down → 3x1x3 pattern**
                        targetPos = pos.add(dx, 0, dz);
                    } else {
                        // **Looking north, south, east, or west → 3x3x1 pattern**
                        targetPos = pos.add(dx, dy, 0);
                        if (facing == Direction.EAST || facing == Direction.WEST) {
                            targetPos = pos.add(0, dy, dz);
                        }
                    }

                    BlockState targetState = world.getBlockState(targetPos);

                    if (!targetState.isAir() && HammerChecks.canToolBreakBlock(tool, targetState)) {
                        maxHardness = Math.max(maxHardness, targetState.getHardness(world, targetPos));
                    }
                }
            }
        }

        return maxHardness;
    }

    private static final Identifier BREAK_SPEED_MODIFIER_ID = HammerMining.id("hammer_slowdown");

    public static void applyMiningSlowdown(ServerPlayerEntity player, int blockCount, float maxHardness, float intialBlockHardness) {

        int efficiencyLevel = EnchantmentHelper.getLevel(HammerHandler.EFFICIENCY, player.getMainHandStack());

        if (blockCount == 0) return;

        if(!(intialBlockHardness <= 29)) return;

        float exhaustion = 0.05f * blockCount;
        player.getHungerManager().addExhaustion(exhaustion);

        double slowFactor = Math.min(0.05 * blockCount + (maxHardness * 0.02), 0.5);
        player.addVelocity(-player.getVelocity().x * slowFactor, 0, -player.getVelocity().z * slowFactor);
        player.velocityModified = true;

        EntityAttributeInstance breakSpeedAttribute = player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
        if (breakSpeedAttribute != null) {
            // **Remove existing modifier first**
            if (breakSpeedAttribute.hasModifier(BREAK_SPEED_MODIFIER_ID)) {
                breakSpeedAttribute.removeModifier(BREAK_SPEED_MODIFIER_ID);
            }

            double basePenalty = (0.08 * blockCount) + (maxHardness * 0.02);
            if (efficiencyLevel > 0 ) {
                basePenalty += efficiencyLevel * 0.05; // Extra slowdown per Efficiency level
            }

            double slowMultiplier = 1.0 - basePenalty;
            slowMultiplier = Math.max(slowMultiplier, 0.15); // Don’t allow it to freeze completely


            EntityAttributeModifier speedModifier = new EntityAttributeModifier(
                    BREAK_SPEED_MODIFIER_ID, slowMultiplier - 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

            breakSpeedAttribute.addPersistentModifier(speedModifier);
        }
    }



    public static void removeMiningSlowdown(ServerPlayerEntity player) {
        EntityAttributeInstance breakSpeedAttribute = player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
        if (breakSpeedAttribute != null && breakSpeedAttribute.hasModifier(BREAK_SPEED_MODIFIER_ID)) {
            breakSpeedAttribute.removeModifier(BREAK_SPEED_MODIFIER_ID);
        }
    }
}
