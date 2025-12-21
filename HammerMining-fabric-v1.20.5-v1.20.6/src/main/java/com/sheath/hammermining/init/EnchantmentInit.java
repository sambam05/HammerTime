package com.sheath.hammermining.init;

import com.sheath.hammermining.HammerMining;
import com.sheath.hammermining.helper.ModLogger;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class EnchantmentInit {

    public static final RegistryKey<Enchantment> HAMMER_KEY =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, HammerMining.id("hammer"));

    private static RegistryKey<Enchantment> of(String path) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, HammerMining.id(path));
    }

    public static void register() {
        ModLogger.info("Registered Hammer Enchantment!");
    }
}
