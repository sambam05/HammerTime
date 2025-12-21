package com.sheath.hammermining.utils.enchantment;

import com.sheath.hammermining.init.EnchantmentInit;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentRegistryHandler {

    public static final Map<String, Enchantment> enchantmentMap = new HashMap<>();

    public static void initializeEnchantments(World world) {
        if (!enchantmentMap.isEmpty()) return;
        enchantmentMap.put("hammer", Registries.ENCHANTMENT.get(EnchantmentInit.HAMMER_KEY.getValue()));
    }

    public static Enchantment getEnchantmentEntry(String key) {
        return enchantmentMap.get(key);
    }

    public static String getEnchantmentKey(Enchantment enchantmentEntry) {
        for (Map.Entry<String, Enchantment> entry : enchantmentMap.entrySet()) {
            if (entry.getValue().equals(enchantmentEntry)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
