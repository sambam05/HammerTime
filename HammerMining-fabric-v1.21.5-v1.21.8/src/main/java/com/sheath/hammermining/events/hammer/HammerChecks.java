package com.sheath.hammermining.events.hammer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;


public class HammerChecks {

    public static boolean checkBlockIsUnbreakable(BlockState state) {
        Block block = state.getBlock();
        String blockId = Registries.BLOCK.getId(block).toString();
        return state.getHardness(null, null) >= 0 && !blockId.equals("minecraft:light");
    }

    public static boolean canToolBreakBlock(ItemStack tool, BlockState blockState) {
        return tool.isSuitableFor(blockState); // Checks if the tool is effective on this block
    }

    public static void checkMiningState() {
        Set<ServerPlayerEntity> toRemove = new HashSet<>();

        for (ServerPlayerEntity player : HammerHandler.miningPlayers) {
            if (!isStillMining(player)) { // **Check if player actually stopped mining**
                HammerSlowdown.removeMiningSlowdown(player);
                toRemove.add(player);
            }
        }

        HammerHandler.miningPlayers.removeAll(toRemove);
    }
    public static boolean isStillMining(ServerPlayerEntity player) {
        return player.isUsingItem() || player.handSwinging;
    }

}
