package com.sheath.hammermining.init;

import com.sheath.hammermining.events.hammer.HammerHandler;
import com.sheath.hammermining.events.hammer.HammerChecks;
import com.sheath.hammermining.helper.ModLogger;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class HammerInit {
    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(HammerHandler::onBlockBreak);
        AttackBlockCallback.EVENT.register(HammerHandler::onStartMining);
        ServerTickEvents.END_SERVER_TICK.register(server -> HammerChecks.checkMiningState());

        ModLogger.info("Registered Hammer Events!");
    }

}
