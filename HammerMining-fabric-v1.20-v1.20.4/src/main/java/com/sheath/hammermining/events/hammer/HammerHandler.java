package com.sheath.hammermining.events.hammer;

import com.sheath.hammermining.init.ConfigInit;
import com.sheath.hammermining.utils.enchantment.EnchantmentChecker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HammerHandler {

    public static final Set<ServerPlayerEntity> miningPlayers = new HashSet<>();

    public static Enchantment EFFICIENCY;

    public static ActionResult onStartMining(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

        if (!EnchantmentChecker.hasRegisteredEnchantment(serverPlayer.getMainHandStack(), world)) {
            return ActionResult.PASS;
        }

        ItemStack tool = serverPlayer.getMainHandStack();
        BlockState state = world.getBlockState(pos);
        float intialBlockHardness = state.getHardness(world,pos);

        if (!HammerChecks.canToolBreakBlock(tool, state)) {
            return ActionResult.PASS;
        }

        float maxHardness = HammerSlowdown.getMaxHardnessIn3x3(world, pos, tool,serverPlayer);
        int surroundingBlocks = countSurroundingBlocks(world, pos);

        if (surroundingBlocks > 0) {
            HammerSlowdown.applyMiningSlowdown(serverPlayer, surroundingBlocks,maxHardness,intialBlockHardness);
            miningPlayers.add(serverPlayer);
        }

        return ActionResult.PASS;
    }

    public static boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        EFFICIENCY = Enchantments.EFFICIENCY;
        Block initalBlock = state.getBlock();

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return true;
        }

        ItemStack tool = serverPlayer.getMainHandStack();
        if (serverPlayer.isSneaking()){
            return true;
        }

        if (!EnchantmentChecker.hasRegisteredEnchantment(tool, world)) {
            return true;
        }

        if(state.getHardness(world, pos) < 0.5f){
            return true;
        }

        break3x3Area((ServerWorld) world, serverPlayer, pos);

        HammerSlowdown.removeMiningSlowdown(serverPlayer);
        miningPlayers.remove(serverPlayer);

        return false;
    }

    public static void break3x3Area(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
        ItemStack heldItem = player.getMainHandStack();
        processBlock(world, pos, player, heldItem, false);
        breakSurroundingBlocks(world, player, pos);
    }

    private static void processBlock(ServerWorld world, BlockPos pos, ServerPlayerEntity player, ItemStack tool, boolean isExtraBlock) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;

        if (!HammerChecks.checkBlockIsUnbreakable(state)) return;

        Enchantment SILK_TOUCH = Enchantments.SILK_TOUCH;
        Enchantment FORTUNE = Enchantments.FORTUNE;

        boolean allowSilk = ConfigInit.LOOT_CONFIG.applySilkTouch;
        boolean allowFortune = ConfigInit.LOOT_CONFIG.applyFortune && !(isExtraBlock && ConfigInit.LOOT_CONFIG.fortuneMainBlockOnly);

        ItemStack dropTool = adjustToolForDrops(tool, SILK_TOUCH, FORTUNE, allowSilk, allowFortune);
        boolean hasSilkTouch = EnchantmentHelper.getLevel(SILK_TOUCH, dropTool) > 0;

        if (ConfigInit.LOOT_CONFIG.splitXpPerBlock) {
            Block.dropStacks(state, world, pos, world.getBlockEntity(pos), player, dropTool);
        } else {
            List<ItemStack> drops;
            if (hasSilkTouch) {
                drops = List.of(new ItemStack(state.getBlock().asItem()));
            } else {
                drops = Block.getDroppedStacks(state, world, pos, world.getBlockEntity(pos), player, dropTool);
            }
            drops.forEach(drop -> Block.dropStack(world, pos, drop));
        }

        world.breakBlock(pos, false);
        state.onStacksDropped(world, pos, tool, true);
        world.playSound(null, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);

        int durabilityCost = 1 + (isExtraBlock ? ConfigInit.LOOT_CONFIG.extraDurabilityPerExtraBlock : 0);
        tool.damage(durabilityCost, world.getRandom(), player);
    }

    private static void breakSurroundingBlocks(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
        Direction facing = player.getHorizontalFacing();
        boolean isVertical = player.getPitch(1.0F) < -45 || player.getPitch(1.0F) > 45;

        List<BlockPos> blocksToBreak = new ArrayList<>();

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

                    if (!targetPos.equals(pos) && !world.getBlockState(targetPos).isAir()) {
                        blocksToBreak.add(targetPos);
                    }
                }
            }
        }

        for (BlockPos blockPos : blocksToBreak) {
            processBlock(world, blockPos, player, player.getMainHandStack(), true);
        }
        HammerSlowdown.removeMiningSlowdown(player);
    }

    public static int countSurroundingBlocks(World world, BlockPos pos) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos targetPos = pos.add(dx, dy, dz);
                    if (!world.getBlockState(targetPos).isAir()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static ItemStack adjustToolForDrops(ItemStack original, Enchantment silk, Enchantment fortune, boolean allowSilk, boolean allowFortune) {
        // 1.20.x fallback: keep the tool as-is for drops.
        return original.copy();
    }
}
