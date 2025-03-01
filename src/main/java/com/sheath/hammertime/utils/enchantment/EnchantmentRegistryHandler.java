package com.sheath.hammertime.utils.enchantment;

import com.sheath.hammertime.init.EnchantmentInit;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentRegistryHandler {

    public static final Map<String, RegistryEntry<Enchantment>> enchantmentMap = new HashMap<>();

    public static void initializeEnchantments(World world) {
        if (!enchantmentMap.isEmpty()) return; // Ensure this runs only once

        RegistryWrapper<Enchantment> enchantmentRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        enchantmentMap.put("hammer", enchantmentRegistry.getOrThrow(EnchantmentInit.HAMMER));

        // Add more as needed
    }

    public static RegistryEntry<Enchantment> getEnchantmentEntry(String key) {

        return enchantmentMap.get(key);
    }

    public static String getEnchantmentKey(RegistryEntry<Enchantment> enchantmentEntry) {
        for (Map.Entry<String, RegistryEntry<Enchantment>> entry : enchantmentMap.entrySet()) {
            if (entry.getValue().equals(enchantmentEntry)) {
                return entry.getKey(); // Return the matching key
            }
        }
        return null; // Return null if not found
    }
}
