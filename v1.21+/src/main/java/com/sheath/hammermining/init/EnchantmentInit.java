package com.sheath.hammermining.init;

import com.mojang.serialization.MapCodec;
import com.sheath.hammermining.HammerMining;
import com.sheath.hammermining.utils.ModLogger;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;

public class EnchantmentInit {

    public static final RegistryKey<Enchantment> HAMMER_KEY =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, HammerMining.id("hammer"));

    private static RegistryKey<Enchantment> of(String path) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, HammerMining.id(path));
    }

    private static <T extends EnchantmentEntityEffect> MapCodec<T> register(String name, MapCodec<T> codec) {
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, HammerMining.id(name), codec);
    }

    public static void register() {
        // Nothing to resolve here anymore — enchantments are dynamically loaded
        ModLogger.info("Registered Hammer Enchantment!");
    }
}
