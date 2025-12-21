package com.sheath.hammermining;

import com.sheath.hammermining.utils.ModLogger;
import com.sheath.hammermining.init.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class HammerMining implements ModInitializer {

    public static final String MOD_ID = "hammermining";



    @Override
    public void onInitialize() {

        //Initializers
        ConfigInit.register();
        HammerInit.register();
        EnchantmentInit.register();
        TradeInit.register();
        LootInit.register();

        ModLogger.info("Registered Everything, Mod loaded!");

    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID,path);
    }
}