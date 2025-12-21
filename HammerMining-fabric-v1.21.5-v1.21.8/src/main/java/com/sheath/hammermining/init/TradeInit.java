package com.sheath.hammermining.init;

import com.sheath.hammermining.helper.ComponentReflect;
import com.sheath.hammermining.helper.ModLogger;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;

import static com.sheath.hammermining.init.ConfigInit.LOOT_CONFIG;


public class TradeInit {

    private static int registered = 0;
    @SuppressWarnings("UnstableApiUsage")
    public static void register() {
        if(!LOOT_CONFIG.tradeEnabled){return;}
        TradeOfferHelper.registerVillagerOffers(
                VillagerProfession.LIBRARIAN,
                1,
                (factories, rebalanced) -> {
                    factories.add((entity, random) -> {
                        if (random.nextFloat() >= LOOT_CONFIG.tradeChance) return null;

                        World world = entity.getWorld();
                        RegistryEntry<Enchantment> hammerEntry = world.getRegistryManager()
                                .getOrThrow(RegistryKeys.ENCHANTMENT)
                                .getOptional(EnchantmentInit.HAMMER_KEY)
                                .orElse(null);

                        if (hammerEntry == null) {
                            ModLogger.warn("⚠️ Hammer enchantment not found for trade");
                            return null;
                        }

                        // Build enchantment component for level 1
                        Map<RegistryEntry<Enchantment>, Integer> enchMap = Map.of(hammerEntry, 1);
                        ItemEnchantmentsComponent component = ComponentReflect.make(enchMap);

                        ItemStack ebook = new ItemStack(Items.ENCHANTED_BOOK, 1);
                        ebook.set(DataComponentTypes.STORED_ENCHANTMENTS, component);

                        TradedItem emeralds = new TradedItem(Items.EMERALD, random.nextBetween(LOOT_CONFIG.tradeMinEmeralds, LOOT_CONFIG.tradeMaxEmeralds));
                        TradedItem book = new TradedItem(Items.BOOK, 1);

                        return new TradeOffer(
                                emeralds,                          // first input: emeralds
                                Optional.of(book),                        // second input: regular book
                                ebook,                                             // output: enchanted book
                                LOOT_CONFIG.tradeMaxUses,                                                // max uses
                                random.nextBetween(3, 10),                        // villager XP
                                0f                                                // price multiplier
                        );
                    });
                    registered++;
                    if (registered <= 1) {
                        ModLogger.info("Registered Hammer trade for librarians!");
                    }
                }

        );
    }
}