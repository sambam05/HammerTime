package com.sheath.hammermining.dataGen.generator;

import com.sheath.hammermining.init.EnchantmentInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.ItemTags;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class EnchantmentGenerator extends FabricDynamicRegistryProvider {
    public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
        System.out.println("Generating Hammer Mining enchantment data...");
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registeries, Entries entries) {
        RegistryWrapper<Item> itemLookup = resolveWrapper(registeries, RegistryKeys.ITEM);
        TagKey<Item> hammerTools = TagKey.of(RegistryKeys.ITEM, com.sheath.hammermining.HammerMining.id("hammer_tools"));
        var toolsTag = itemLookup.getOptional(hammerTools).orElse(itemLookup.getOrThrow(ItemTags.PICKAXES));

        // Register the enchantment "Vein Miner"
        entries.add(EnchantmentInit.HAMMER_KEY, Enchantment.builder(
                        Enchantment.definition(
                                        toolsTag,
                                        toolsTag,
                                        12, // Weight (enchantment table rarity)
                                        1, // Max level
                                        Enchantment.leveledCost(15, 5), // Min cost
                                        Enchantment.leveledCost(30, 5), // Max cost
                                        5, // Anvil cost
                                        AttributeModifierSlot.HAND
                                )
                        )
                .build(EnchantmentInit.HAMMER_KEY.getValue())
        );
    }

    @Override
    public String getName() {
        return "HammerMiningEnchantmentGenerator";
    }

    @SuppressWarnings("unchecked")
    private static <T> RegistryWrapper<T> resolveWrapper(RegistryWrapper.WrapperLookup registries, RegistryKey<?> key) {
        for (String methodName : new String[]{"getWrapperOrThrow", "getOrThrow", "getWrapper"}) {
            try {
                Method method = registries.getClass().getMethod(methodName, RegistryKey.class);
                Object result = method.invoke(registries, key);
                if (result instanceof RegistryWrapper<?>) {
                    return (RegistryWrapper<T>) result;
                }
            } catch (Exception ignored) {
            }
        }
        throw new IllegalStateException("Unable to access registry: " + key.getValue());
    }
}
