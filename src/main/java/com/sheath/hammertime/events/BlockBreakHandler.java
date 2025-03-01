package com.sheath.hammertime.events;

import com.sheath.hammertime.Hammertime;
import com.sheath.hammertime.utils.enchantment.EnchantmentChecker;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class BlockBreakHandler {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(BlockBreakHandler::onBlockBreak);
    }

    public static boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return true;
        ItemStack tool = serverPlayer.getMainHandStack();

        if (!EnchantmentChecker.hasRegisteredEnchantment(tool,world)) {
            return true;
        }
        break3x3Area((ServerWorld) world, serverPlayer, pos);
        return false;
    }

    public static void break3x3Area(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
        ItemStack heldItem = player.getMainHandStack();

        // Get the direction the player is facing
        Direction facing = player.getHorizontalFacing();
        boolean isVertical = player.getPitch(1.0F) < -45 || player.getPitch(1.0F) > 45; // Detect up/down

        // **Break the initial block first**
        processBlock(world, pos, player, heldItem);

        // **Determine the breaking shape**
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos targetPos;

                    if (isVertical) { // Looking up or down → 3x1x3
                        targetPos = pos.add(dx, 0, dz);
                    } else { // Looking north, south, east, or west → 3x3x1
                        targetPos = pos.add(dx, dy, 0);
                        if (facing == Direction.EAST || facing == Direction.WEST) {
                            targetPos = pos.add(0, dy, dz);
                        }
                    }

                    // Skip the initial block position
                    if (!targetPos.equals(pos)) {
                        processBlock(world, targetPos, player, heldItem);
                    }
                }
            }
        }
    }

    private static void processBlock(ServerWorld world, BlockPos pos, ServerPlayerEntity player, ItemStack tool) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;

        RegistryEntry<Enchantment> SILK_TOUCH = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        RegistryEntry<Enchantment> FORTUNE = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);

        int fortuneLevel = EnchantmentHelper.getLevel(FORTUNE, tool);
        boolean hasSilkTouch = EnchantmentHelper.getLevel(SILK_TOUCH, tool) > 0;

        List<ItemStack> drops;
        if (hasSilkTouch) {
            drops = List.of(new ItemStack(state.getBlock().asItem())); // Silk Touch: Drop the block itself
        } else {
            drops = Block.getDroppedStacks(state, world, pos, world.getBlockEntity(pos), player, tool);
            if (fortuneLevel > 0) {
                drops.forEach(drop -> drop.setCount(drop.getCount() + world.random.nextInt(fortuneLevel + 1))); // Apply Fortune
            }
        }

        world.breakBlock(pos, false);
        drops.forEach(drop -> Block.dropStack(world, pos, drop));
        state.onStacksDropped(world, pos, tool, true);
        world.playSound(null, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
    }
}

