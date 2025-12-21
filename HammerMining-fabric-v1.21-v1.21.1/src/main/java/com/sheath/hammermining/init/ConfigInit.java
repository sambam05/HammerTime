package com.sheath.hammermining.init;

import com.sheath.hammermining.config.GeneralConfig;
import com.sheath.hammermining.utils.ModLogger;

public class ConfigInit {

    public static GeneralConfig LOOT_CONFIG;

    public static void register() {
        LOOT_CONFIG = GeneralConfig.load();
        ModLogger.info("Config Loaded!");
    }
}
