package com.sheath.hammermining.helper;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;

import java.lang.reflect.Constructor;
import java.util.Map;

public class ComponentReflect {
    private static final Constructor<ItemEnchantmentsComponent> enchantCtor;

    static {
        try {
            // grab the package-private constructor (Map, boolean)
            enchantCtor = ItemEnchantmentsComponent.class
                    .getDeclaredConstructor(Object2IntOpenHashMap.class, boolean.class);
            enchantCtor.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access ItemEnchantmentsComponent ctor", e);
        }
    }

    /** Build an ItemEnchantmentsComponent with the given map. */
    public static ItemEnchantmentsComponent make(Map<? extends RegistryEntry<Enchantment>, Integer> map) {
        try {
            var fastMap = new Object2IntOpenHashMap<RegistryEntry<Enchantment>>(map);
            return enchantCtor.newInstance(fastMap, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate ItemEnchantmentsComponent", e);
        }
    }
}

