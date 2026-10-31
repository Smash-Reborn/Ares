package sh.smsh.ares.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import sh.smsh.ares.AresConstants;
import sh.smsh.ares.AresLog;
import sh.smsh.ares.fabric.combat.LegacyDamageFeedback;
import sh.smsh.ares.fabric.command.AresClientCommands;
import sh.smsh.ares.fabric.debug.SpawnPacketDebug;
import sh.smsh.ares.fabric.disguise.DisguiseDetectionController;
import sh.smsh.ares.fabric.gui.AresGuiScheduler;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.forge.ForgeFeatureBootstrap;

public final class AresFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ForgeFeatureBootstrap.init(FabricLoader.getInstance().getGameDir().toFile());
        FabricServerProbe.register();
        AresClientCommands.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AresGuiScheduler.tick();
            FeatureManager.onClientTick();
            DisguiseDetectionController.onClientTick();
            SpawnPacketDebug.flush();
            LegacyDamageFeedback.captureHealth();
        });
        AresLog.LOGGER.info(AresConstants.MOD_NAME + " loaded.");
    }
}
