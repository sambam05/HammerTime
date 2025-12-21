package com.sheath.hammermining.utils.enchantment;

import com.sheath.hammermining.init.EnchantmentInit;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

public class EnchantmentChecker {

    public static boolean hasRegisteredEnchantment(ItemStack tool, World world) {
        if (tool == null || tool.isEmpty()) {
            return false;
        }
        Enchantment hammer = Registries.ENCHANTMENT.get(EnchantmentInit.HAMMER_KEY.getValue());
        if (hammer == null) {
            return false;
        }
        return EnchantmentHelper.getLevel(hammer, tool) > 0;
    }

    public static String getCustomEnchantment(ItemStack tool) {
        for (Enchantment enchantment : EnchantmentRegistryHandler.enchantmentMap.values()) {
            if (EnchantmentHelper.getLevel(enchantment, tool) > 0) {
                return EnchantmentRegistryHandler.getEnchantmentKey(enchantment);
            }
        }
        return null;
    }
}
