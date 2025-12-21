package com.sheath.hammermining.events.hammer;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class HammerSlowdown {

    public static float getMaxHardnessIn3x3(World world, BlockPos pos, ItemStack tool, PlayerEntity player) {
        float maxHardness = 0.0f;

        Direction facing = player.getHorizontalFacing();
        boolean isVertical = player.getPitch(1.0F) < -45 || player.getPitch(1.0F) > 45;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos targetPos;

                    if (isVertical) {
                        targetPos = pos.add(dx, 0, dz);
                    } else {
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

    public static void applyMiningSlowdown(ServerPlayerEntity player, int blockCount, float maxHardness, float intialBlockHardness) {
        // 1.20.x: skip slowdown because the block break speed attribute is not available here.
    }

    public static void removeMiningSlowdown(ServerPlayerEntity player) {
        // No-op for 1.20.x
    }
}
