package com.sheath.hammertime.utils.enchantment;

import com.sheath.hammertime.init.EnchantmentInit;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public class EnchantmentChecker {


    public static boolean hasRegisteredEnchantment(ItemStack tool, World world) {
        RegistryEntry<Enchantment> enchantmentEntry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(EnchantmentInit.HAMMER);
        if (EnchantmentHelper.getLevel(enchantmentEntry, tool) > 0) {
            return true; // Found at least one registered enchantment
        }
        return false; // No registered enchantments found
    }

    public static String getCustomEnchantment(ItemStack tool) {
        for (RegistryEntry<Enchantment> enchantmentEntry : EnchantmentRegistryHandler.enchantmentMap.values()) {
            if (EnchantmentHelper.getLevel(enchantmentEntry, tool) > 0) {

                return EnchantmentRegistryHandler.getEnchantmentKey(enchantmentEntry); // Return the first found enchantment
            }
        }
        return null; // No enchantments found
    }




}
