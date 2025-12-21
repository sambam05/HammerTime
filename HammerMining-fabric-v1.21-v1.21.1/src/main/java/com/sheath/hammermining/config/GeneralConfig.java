package com.sheath.hammermining.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.sheath.hammermining.helper.ModLogger;

import java.io.File;
import java.util.List;

public class GeneralConfig {

    public boolean applyToAllBlocks = true;
    public List<String> blockWhitelist = List.of(); // e.g., "minecraft:diamond_ore"
    public List<String> blockBlacklist = List.of(); // ignored if applyToAllBlocks is true
    public int miningSpeedScale = 1; // 1 = normal speed, 100 = slowest
    public int extraDurabilityPerExtraBlock = 0; // additional damage applied per bonus block broken
    public boolean splitXpPerBlock = true; // drop XP per affected block (vanilla pacing)
    public boolean applySilkTouch = true; // allow Silk Touch to affect Hammer drops
    public boolean applyFortune = true; // allow Fortune to affect Hammer drops
    public boolean fortuneMainBlockOnly = true; // if true, Fortune applies to main block only (prevents multi-block double-dip)

    public boolean tradeEnabled = true;
    public float tradeChance = 0.05f;
    public int tradeMinEmeralds = 16;
    public int tradeMaxEmeralds = 64;
    public int tradeMaxUses = 3;

    public boolean lootEnabled = true;
    // Chance a Hammer book appears in targeted loot tables (0.0 - 1.0). Default lowered to reduce book spam.
    public float dropChance = 0.05f;
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
            ModLogger.warn("Failed to create config directory: {}", configDir);
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
                config.dropChance = clamp01(((Number) fileConfig.getOrElse("LootChests.dropChance", config.dropChance)).floatValue());
                config.lootTableTargets = fileConfig.getOrElse("LootChests.lootTableTargets", config.lootTableTargets);

                // Mining tweaks
                config.extraDurabilityPerExtraBlock = fileConfig.getOrElse("Mining.extraDurabilityPerExtraBlock", config.extraDurabilityPerExtraBlock);
                config.splitXpPerBlock = fileConfig.getOrElse("Mining.splitXpPerBlock", config.splitXpPerBlock);
                config.applySilkTouch = fileConfig.getOrElse("Mining.applySilkTouch", config.applySilkTouch);
                config.applyFortune = fileConfig.getOrElse("Mining.applyFortune", config.applyFortune);
                config.fortuneMainBlockOnly = fileConfig.getOrElse("Mining.fortuneMainBlockOnly", config.fortuneMainBlockOnly);

            } else {
                writeConfig(fileConfig, config);
            }
            // Always rewrite comments and current values so users see descriptions
            writeConfig(fileConfig, config);
            fileConfig.save();

        }

        return config;
    }

    public void save() {
        try (CommentedFileConfig fileConfig = CommentedFileConfig.builder(CONFIG_FILE)
                .autosave()
                .sync()
                .preserveInsertionOrder()
                .build()) {
            fileConfig.load();

            writeConfig(fileConfig, this);
            fileConfig.save();
        } catch (Exception e) {
            ModLogger.warn("Failed to save config: {}", e.getMessage());
        }
    }

    private static void writeConfig(CommentedFileConfig fileConfig, GeneralConfig values) {
        // VillagerTrades
        fileConfig.set("VillagerTrades.enabled", values.tradeEnabled);
        fileConfig.setComment("VillagerTrades.enabled", "Enable the librarian hammer enchantment book trade (true/false).");

        fileConfig.set("VillagerTrades.tradeChance", values.tradeChance);
        fileConfig.setComment("VillagerTrades.tradeChance", "Chance the librarian offers the hammer trade (0.0-1.0). Higher = more likely, not guaranteed.");

        fileConfig.set("VillagerTrades.tradeMinEmeralds", values.tradeMinEmeralds);
        fileConfig.setComment("VillagerTrades.tradeMinEmeralds", "Minimum emerald cost for the trade.");

        fileConfig.set("VillagerTrades.tradeMaxEmeralds", values.tradeMaxEmeralds);
        fileConfig.setComment("VillagerTrades.tradeMaxEmeralds", "Maximum emerald cost for the trade.");

        fileConfig.set("VillagerTrades.tradeMaxUses", values.tradeMaxUses);
        fileConfig.setComment("VillagerTrades.tradeMaxUses", "How many times the trade can be used before locking.");

        // LootChests
        fileConfig.set("LootChests.enabled", values.lootEnabled);
        fileConfig.setComment("LootChests.enabled", "Enable Hammer enchantment book drops in loot chests (true/false).");

        float sanitizedDrop = clamp01(values.dropChance);
        fileConfig.set("LootChests.dropChance", sanitizedDrop);
        fileConfig.setComment("LootChests.dropChance", "Chance a Hammer book drops from targeted loot tables (0.0-1.0). Lower this if books feel too common.");

        fileConfig.set("LootChests.lootTableTargets", values.lootTableTargets);
        fileConfig.setComment("LootChests.lootTableTargets", "List of loot table IDs where Hammer books can appear.");

        // Mining
        fileConfig.set("Mining.extraDurabilityPerExtraBlock", values.extraDurabilityPerExtraBlock);
        fileConfig.setComment("Mining.extraDurabilityPerExtraBlock", "Extra durability damage per bonus block broken (0 = vanilla cost only). Higher values mean Hammers wear down faster.");

        fileConfig.set("Mining.splitXpPerBlock", values.splitXpPerBlock);
        fileConfig.setComment("Mining.splitXpPerBlock", "If true, XP orbs drop from every broken block (vanilla pacing). If false, only the first block drops XP.");

        fileConfig.set("Mining.applySilkTouch", values.applySilkTouch);
        fileConfig.setComment("Mining.applySilkTouch", "If true, Silk Touch on your tool applies to Hammer drops. Turn off to force normal drops.");

        fileConfig.set("Mining.applyFortune", values.applyFortune);
        fileConfig.setComment("Mining.applyFortune", "If true, Fortune on your tool applies to Hammer drops. Turn off to keep drops closer to vanilla.");

        fileConfig.set("Mining.fortuneMainBlockOnly", values.fortuneMainBlockOnly);
        fileConfig.setComment("Mining.fortuneMainBlockOnly", "If true, Fortune applies only to the original block; extra blocks ignore Fortune to prevent double-dipping.");
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }


}
