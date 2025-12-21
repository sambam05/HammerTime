package com.sheath.hammermining.init;

import com.sheath.hammermining.config.GeneralConfig;
import com.sheath.hammermining.helper.ModLogger;

public class ConfigInit {

    public static GeneralConfig LOOT_CONFIG;

    public static void register() {
        LOOT_CONFIG = GeneralConfig.load();
        ModLogger.info("Config Loaded!");
    }
}
