package sh.smsh.ares.forge;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.forge.disguise.DisguiseDetectionController;
import sh.smsh.ares.forge.gui.AresGuiScheduler;

public class ForgeClientTickHandler {

    @SubscribeEvent
    public void onClientTick(@NotNull TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        FeatureManager.onClientTick();
        DisguiseDetectionController.onClientTick();
        AresGuiScheduler.openRequestedScreen();
    }
}