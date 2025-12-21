package com.sheath.hammermining.utils.enchantment;

import com.sheath.hammermining.init.EnchantmentInit;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EnchantmentRegistryHandler {

    public static final Map<String, RegistryEntry<Enchantment>> enchantmentMap = new HashMap<>();

    public static void initializeEnchantments(World world) {
        if (!enchantmentMap.isEmpty()) return; // Ensure this runs only once

        RegistryWrapper<Enchantment> wrapper = getWrapper(world);
        enchantmentMap.put("hammer", wrapper.getOptional(EnchantmentInit.HAMMER_KEY).orElseThrow());

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

    @SuppressWarnings("unchecked")
    private static RegistryWrapper<Enchantment> getWrapper(World world) {
        Object mgr = world.getRegistryManager();
        for (String methodName : new String[]{"getWrapperOrThrow", "getOrThrow", "getWrapper"}) {
            try {
                Method m = mgr.getClass().getMethod(methodName, RegistryKey.class);
                Object res = m.invoke(mgr, RegistryKeys.ENCHANTMENT);
                if (res instanceof RegistryWrapper) {
                    return (RegistryWrapper<Enchantment>) res;
                }
            } catch (Exception ignored) {
            }
        }
        throw new IllegalStateException("Unable to access enchantment registry");
    }
}
