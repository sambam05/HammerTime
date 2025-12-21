package com.sheath.hammermining.config;


import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.sheath.hammermining.utils.ModLogger;

import java.io.File;
import java.util.List;

public class GeneralConfig {

    public boolean tradeEnabled = true;
    public float tradeChance = 0.05f;
    public int tradeMinEmeralds = 16;
    public int tradeMaxEmeralds = 64;
    public int tradeMaxUses = 3;

    public boolean lootEnabled = true;
    public float dropChance = 0.25f;
    public List<String> lootTableTargets = List.of(
            "minecraft:chests/abandoned_mineshaft", "minecraft:chests/ancient_city",
            "minecraft:chests/ancient_city_ice_box", "minecraft:chests/bastion_bridge", "minecraft:chests/bastion_hoglin_stable",
            "minecraft:chests/bastion_other", "minecraft:chests/bastion_treasure", "minecraft:chests/buried_treasure",
            "minecraft:chests/desert_pyramid", "minecraft:chests/end_city_treasure", "minecraft:chests/igloo_chest",
            "minecraft:chests/jungle_temple", "minecraft:chests/jungle_temple_dispenser", "minecraft:chests/nether_bridge",
            "minecraft:chests/pillager_outpost", "minecraft:chests/ruined_portal", "minecraft:chests/shipwreck_map",
            "minecraft:chests/shipwreck_supply", "minecraft:chests/shipwreck_treasure", "minecraft:chests/simple_dungeon",
            "minecraft:chests/spawn_bonus_chest", "minecraft:chests/stronghold_corridor", "minecraft:chests/stronghold_crossing",
            "minecraft:chests/stronghold_library", "minecraft:chests/trial_chambers/corridor", "minecraft:chests/trial_chambers/entrance",
            "minecraft:chests/trial_chambers/intersection", "minecraft:chests/trial_chambers/intersection_barrel", "minecraft:chests/trial_chambers/reward",
            "minecraft:chests/trial_chambers/reward_common", "minecraft:chests/trial_chambers/reward_ominous", "minecraft:chests/trial_chambers/reward_ominous_common",
            "minecraft:chests/trial_chambers/reward_ominous_rare", "minecraft:chests/trial_chambers/reward_ominous_unique", "minecraft:chests/trial_chambers/reward_rare",
            "minecraft:chests/trial_chambers/reward_unique", "minecraft:chests/trial_chambers/supply", "minecraft:chests/underwater_ruin_big",
            "minecraft:chests/underwater_ruin_small", "minecraft:chests/village/village_armorer", "minecraft:chests/village/village_butcher",
            "minecraft:chests/village/village_cartographer", "minecraft:chests/village/village_desert_house", "minecraft:chests/village/village_fisher",
            "minecraft:chests/village/village_fletcher", "minecraft:chests/village/village_mason", "minecraft:chests/village/village_plains_house",
            "minecraft:chests/village/village_savanna_house", "minecraft:chests/village/village_shepherd", "minecraft:chests/village/village_snowy_house",
            "minecraft:chests/village/village_taiga_house", "minecraft:chests/village/village_tannery", "minecraft:chests/village/village_temple",
            "minecraft:chests/village/village_toolsmith", "minecraft:chests/village/village_weaponsmith", "minecraft:chests/woodland_mansion"
    );
    private static final File CONFIG_FILE = new File("config/HammerMining/General_Config.toml");

    public static GeneralConfig load() {
        GeneralConfig config = new GeneralConfig();

        File configDir = CONFIG_FILE.getParentFile();
        if (!configDir.exists() && !configDir.mkdirs()) {
            ModLogger.warn("⚠ Failed to create config directory: {}", configDir);
        }

        try (CommentedFileConfig fileConfig = CommentedFileConfig.builder(CONFIG_FILE)
                .autosave()
                .sync()
                .preserveInsertionOrder()
                .build()) {

            if (CONFIG_FILE.exists()) {
                fileConfig.load();

                // VillagerTrades
                config.tradeEnabled = fileConfig.getOrElse("VillagerTrades.enabled", config.tradeEnabled);
                config.tradeChance = ((Number) fileConfig.getOrElse("VillagerTrades.tradeChance", config.tradeChance)).floatValue();
                config.tradeMinEmeralds = fileConfig.getOrElse("VillagerTrades.tradeMinEmeralds", config.tradeMinEmeralds);
                config.tradeMaxEmeralds = fileConfig.getOrElse("VillagerTrades.tradeMaxEmeralds", config.tradeMaxEmeralds);
                config.tradeMaxUses = fileConfig.getOrElse("VillagerTrades.tradeMaxUses", config.tradeMaxUses);

                // LootChests
                config.lootEnabled = fileConfig.getOrElse("LootChests.enabled", config.lootEnabled);
                config.dropChance = ((Number) fileConfig.getOrElse("LootChests.dropChance", config.dropChance)).floatValue();
                config.lootTableTargets = fileConfig.getOrElse("LootChests.lootTableTargets", config.lootTableTargets);

            } else {
                // ===== VillagerTrades Section =====
                fileConfig.set("VillagerTrades.enabled", config.tradeEnabled);
                fileConfig.setComment("VillagerTrades.enabled", "Enable the librarian hammer enchantment book trade");

                fileConfig.set("VillagerTrades.tradeChance", config.tradeChance);
                fileConfig.setComment("VillagerTrades.tradeChance", "Chance the librarian offers the hammer enchantment trade (0.0–1.0).  1.0 is not 100%, it just increases it.");

                fileConfig.set("VillagerTrades.tradeMinEmeralds", config.tradeMinEmeralds);
                fileConfig.setComment("VillagerTrades.tradeMinEmeralds", "Minimum number of emeralds required for the trade");

                fileConfig.set("VillagerTrades.tradeMaxEmeralds", config.tradeMaxEmeralds);
                fileConfig.setComment("VillagerTrades.tradeMaxEmeralds", "Maximum number of emeralds required for the trade");

                fileConfig.set("VillagerTrades.tradeMaxUses", config.tradeMaxUses);
                fileConfig.setComment("VillagerTrades.tradeMaxUses", "How many times the trade can be used before locking");

                // ===== LootChests Section =====
                fileConfig.set("LootChests.enabled", config.lootEnabled);
                fileConfig.setComment("LootChests.enabled", "Enable hammer enchantment book drops in loot chests");

                fileConfig.set("LootChests.dropChance", config.dropChance);
                fileConfig.setComment("LootChests.dropChance", "Chance a hammer enchantment book drops from loot chests (0.0–1.0)");

                fileConfig.set("LootChests.lootTableTargets", config.lootTableTargets);
                fileConfig.setComment("LootChests.lootTableTargets", "List of loot table IDs where hammer enchantment books can appear");

                fileConfig.save();
            }

        }

        return config;
    }
}