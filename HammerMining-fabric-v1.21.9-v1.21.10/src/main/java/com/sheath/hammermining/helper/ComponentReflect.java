package com.sheath.hammermining.helper;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;

import java.lang.reflect.Constructor;
import java.util.Map;

public class ComponentReflect {
    private static final Constructor<ItemEnchantmentsComponent> enchantCtor;
    private static final Constructor<ItemEnchantmentsComponent.Builder> builderCtor;

    static {
        Constructor<ItemEnchantmentsComponent> tmpEnchant = null;
        Constructor<ItemEnchantmentsComponent.Builder> tmpBuilder = null;

        // Try modern Builder first (preferred on newer MC versions)
        try {
            tmpBuilder = ItemEnchantmentsComponent.Builder.class
                    .getDeclaredConstructor(ItemEnchantmentsComponent.class);
            tmpBuilder.setAccessible(true);
        } catch (Exception ignored) {
            // builder might not exist; fall back to direct ctor
        }

        // Try known ctor signatures for direct construction
        for (Class<?>[] sig : new Class[][]{
                {Object2IntOpenHashMap.class, boolean.class},
                {Object2IntOpenHashMap.class}
        }) {
            if (tmpEnchant != null) break;
            try {
                Constructor<ItemEnchantmentsComponent> candidate =
                        ItemEnchantmentsComponent.class.getDeclaredConstructor(sig);
                candidate.setAccessible(true);
                tmpEnchant = candidate;
            } catch (Exception ignored) {
            }
        }

        if (tmpBuilder == null && tmpEnchant == null) {
            throw new RuntimeException("Failed to access ItemEnchantmentsComponent constructors");
        }

        builderCtor = tmpBuilder;
        enchantCtor = tmpEnchant;
    }

    /** Build an ItemEnchantmentsComponent with the given map. */
    public static ItemEnchantmentsComponent make(Map<? extends RegistryEntry<Enchantment>, Integer> map) {
        Object2IntOpenHashMap<RegistryEntry<Enchantment>> fastMap = new Object2IntOpenHashMap<>(map);

        // Preferred: builder path
        if (builderCtor != null) {
            try {
                ItemEnchantmentsComponent.Builder builder = builderCtor.newInstance(ItemEnchantmentsComponent.DEFAULT);
                fastMap.object2IntEntrySet().forEach(entry -> builder.set(entry.getKey(), entry.getIntValue()));
                return builder.build();
            } catch (Exception ignored) {
                // fall through
            }
        }

        // Fallback: direct ctor path
        try {
            if (enchantCtor.getParameterCount() == 2) {
                return enchantCtor.newInstance(fastMap, true);
            }
            return enchantCtor.newInstance(fastMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate ItemEnchantmentsComponent", e);
        }
    }
}
