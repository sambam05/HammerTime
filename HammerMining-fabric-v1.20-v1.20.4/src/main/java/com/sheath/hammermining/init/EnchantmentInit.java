package com.sheath.hammermining.init;

import com.sheath.hammermining.HammerMining;
import com.sheath.hammermining.helper.ModLogger;
import com.sheath.hammermining.utils.enchantment.HammerEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class EnchantmentInit {

    public static Enchantment HAMMER;

    public static final RegistryKey<Enchantment> HAMMER_KEY =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, HammerMining.id("hammer"));

    private static RegistryKey<Enchantment> of(String path) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, HammerMining.id(path));
    }

    public static void register() {
        HAMMER = Registry.register(Registries.ENCHANTMENT, HAMMER_KEY.getValue(), new HammerEnchantment());
        ModLogger.info("Registered Hammer Enchantment!");
    }
}
