package com.sheath.hammertime.events;

import com.sheath.hammertime.Hammertime;
import com.sheath.hammertime.utils.enchantment.EnchantmentChecker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HammerHandler {

    private static final Set<ServerPlayerEntity> miningPlayers = new HashSet<>();

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(HammerHandler::onBlockBreak);
        AttackBlockCallback.EVENT.register(HammerHandler::onStartMining);
        ServerTickEvents.END_SERVER_TICK.register(server -> checkMiningState());
    }

    private static ActionResult onStartMining(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

        if (!EnchantmentChecker.hasRegisteredEnchantment(serverPlayer.getMainHandStack(), world)) {
            return ActionResult.PASS;
        }

        ItemStack tool = serverPlayer.getMainHandStack();
        BlockState state = world.getBlockState(pos);

        // **Check if tool can break the block**
        if (!canToolBreakBlock(tool, state)) {
            return ActionResult.PASS; // Don't slow down if the tool isn't suitable
        }

        // **Find the hardest block in the 3x3 area**
        float maxHardness = getMaxHardnessIn3x3(world, pos, tool,serverPlayer);
        int surroundingBlocks = countSurroundingBlocks(world, pos);

        if (surroundingBlocks > 0) {
            applyMiningSlowdown(serverPlayer, surroundingBlocks,maxHardness);
            miningPlayers.add(serverPlayer); // **Track player as actively mining**
        }

        // **Apply mining slowdown immediately**

        return ActionResult.PASS;
    }

    public static boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return true;
        ItemStack tool = serverPlayer.getMainHandStack();

        if (!EnchantmentChecker.hasRegisteredEnchantment(tool, world)) {
            return true;
        }

        break3x3Area((ServerWorld) world, serverPlayer, pos);

        // **Ensure mining slowdown is removed after mining finishes**
        removeMiningSlowdown(serverPlayer);
        miningPlayers.remove(serverPlayer); // **Ensure player is removed from tracking**

        return false;
    }

    public static void break3x3Area(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
        ItemStack heldItem = player.getMainHandStack();

        // **Break the initial block first**
        processBlock(world, pos, player, heldItem);

        // **Break surrounding blocks**
        breakSurroundingBlocks(world, player, pos);
    }

    private static void processBlock(ServerWorld world, BlockPos pos, ServerPlayerEntity player, ItemStack tool) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;

        if (!canBreak(state)) return;

        // **Use Registry-based Enchantment Lookup**
        RegistryEntry<Enchantment> SILK_TOUCH = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        RegistryEntry<Enchantment> FORTUNE = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);

        int fortuneLevel = EnchantmentHelper.getLevel(FORTUNE, tool);
        boolean hasSilkTouch = EnchantmentHelper.getLevel(SILK_TOUCH, tool) > 0;

        List<ItemStack> drops;
        if (hasSilkTouch) {
            drops = List.of(new ItemStack(state.getBlock().asItem()));
        } else {
            drops = Block.getDroppedStacks(state, world, pos, world.getBlockEntity(pos), player, tool);
            if (fortuneLevel > 0) {
                drops.forEach(drop -> drop.setCount(drop.getCount() + world.random.nextInt(fortuneLevel + 1)));
            }
        }

        world.breakBlock(pos, false);
        drops.forEach(drop -> Block.dropStack(world, pos, drop));
        state.onStacksDropped(world, pos, tool, true);
        world.playSound(null, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);

        // **Apply tool durability loss**
        tool.damage(1, player);
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

        // **Break Blocks**
        for (BlockPos blockPos : blocksToBreak) {
            processBlock(world, blockPos, player, player.getMainHandStack());
        }

        removeMiningSlowdown(player);

    }

    private static float getMaxHardnessIn3x3(World world, BlockPos pos, ItemStack tool, PlayerEntity player) {
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

                    if (!targetState.isAir() && canToolBreakBlock(tool, targetState)) {
                        maxHardness = Math.max(maxHardness, targetState.getHardness(world, targetPos));
                    }
                }
            }
        }

        return maxHardness;
    }



    private static final Identifier BREAK_SPEED_MODIFIER_ID = Hammertime.id("hammer_slowdown");

    private static void applyMiningSlowdown(ServerPlayerEntity player, int blockCount, float maxHardness) {
        if (blockCount == 0) return;

        float exhaustion = 0.2f * blockCount;
        player.getHungerManager().addExhaustion(exhaustion);

        double slowFactor = Math.min(0.05 * blockCount + (maxHardness * 0.02), 0.5);
        player.addVelocity(-player.getVelocity().x * slowFactor, 0, -player.getVelocity().z * slowFactor);
        player.velocityModified = true;

        EntityAttributeInstance breakSpeedAttribute = player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED);
        if (breakSpeedAttribute != null) {
            // **Remove existing modifier first**
            if (breakSpeedAttribute.hasModifier(BREAK_SPEED_MODIFIER_ID)) {
                breakSpeedAttribute.removeModifier(BREAK_SPEED_MODIFIER_ID);
            }

            double slowMultiplier = 1.0 - ((0.05 * blockCount) + (maxHardness * 0.01));
            slowMultiplier = Math.max(slowMultiplier, 0.3); // Prevent mining from being too slow

            EntityAttributeModifier speedModifier = new EntityAttributeModifier(
                    BREAK_SPEED_MODIFIER_ID, slowMultiplier - 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

            breakSpeedAttribute.addPersistentModifier(speedModifier);
        }
    }



    private static void removeMiningSlowdown(ServerPlayerEntity player) {
        EntityAttributeInstance breakSpeedAttribute = player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED);
        if (breakSpeedAttribute != null && breakSpeedAttribute.hasModifier(BREAK_SPEED_MODIFIER_ID)) {
            breakSpeedAttribute.removeModifier(BREAK_SPEED_MODIFIER_ID);
        }
    }
    private static int countSurroundingBlocks(World world, BlockPos pos) {
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

    private static boolean canBreak(BlockState state) {
        Block block = state.getBlock();
        String blockId = Registries.BLOCK.getId(block).toString();
        return state.getHardness(null, null) >= 0 && !blockId.equals("minecraft:light");
    }

    private static boolean canToolBreakBlock(ItemStack tool, BlockState blockState) {
        return tool.isSuitableFor(blockState); // Checks if the tool is effective on this block
    }

    private static void checkMiningState() {
        Set<ServerPlayerEntity> toRemove = new HashSet<>();

        for (ServerPlayerEntity player : miningPlayers) {
            if (!isStillMining(player)) { // **Check if player actually stopped mining**
                removeMiningSlowdown(player);
                toRemove.add(player);
            }
        }

        miningPlayers.removeAll(toRemove);
    }
    private static boolean isStillMining(ServerPlayerEntity player) {
        return player.isUsingItem() || player.handSwinging;
    }



}
