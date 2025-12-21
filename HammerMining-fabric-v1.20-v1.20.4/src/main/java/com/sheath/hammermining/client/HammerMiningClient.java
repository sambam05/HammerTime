package com.sheath.hammermining.client;

import com.sheath.hammermining.helper.ModLogger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Environment(EnvType.CLIENT)
public class HammerMiningClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.isIntegratedServerRunning()) {
                ModLogger.info("Singleplayer detected, server-side handlers already registered.");
            }
        });
    }
}

