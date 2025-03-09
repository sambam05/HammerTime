package com.sheath.hammertime;

import com.sheath.hammertime.events.HammerHandler;
import com.sheath.hammertime.init.EnchantmentInit;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hammertime implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Hammer Time");

    public static final String MOD_ID = "hammertime";

    @Override
    public void onInitialize() {

        EnchantmentInit.register();

        HammerHandler.register();

    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID,path);
    }
}
