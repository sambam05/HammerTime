package com.sheath.hammermining.init;

import com.sheath.hammermining.helper.ComponentReflect;
import com.sheath.hammermining.utils.ModLogger;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;

import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetComponentsLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;

import static com.sheath.hammermining.init.ConfigInit.LOOT_CONFIG;


public class LootInit {

    public static void register() {
        if(!LOOT_CONFIG.lootEnabled){return;}
        LootTableEvents.MODIFY.register((tableKey, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) return;

            if (!LOOT_CONFIG.lootTableTargets.contains(tableKey.getValue().toString())) return;

            // Lookup the live registry for your HAMMER enchantment
            RegistryWrapper<Enchantment> enchantWrapper =
                    registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
            RegistryEntry<Enchantment> hammerEntry =
                    enchantWrapper.getOptional(EnchantmentInit.HAMMER_KEY).orElse(null);

            if (hammerEntry == null) {
                ModLogger.error("Cannot inject loot — HAMMER enchantment not found");
                return;
            }


            // Build the component map <HAMMER entry → level 1>
            Map<RegistryEntry<Enchantment>, Integer> enchMap = Map.of(hammerEntry, 1);
            ItemEnchantmentsComponent component = ComponentReflect.make(enchMap);

            // Create and inject the loot pool
            LootPool pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(RandomChanceLootCondition.builder(LOOT_CONFIG.dropChance).build())
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                            .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1)))
                            .apply(SetComponentsLootFunction.builder(
                                    DataComponentTypes.STORED_ENCHANTMENTS,
                                    component
                            )))
                    .build();

            tableBuilder.pool(pool);

        });
        ModLogger.info("Registered Hammer loot in chests!");
    }
}

