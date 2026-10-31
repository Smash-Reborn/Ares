package sh.smsh.ares.forge.gui;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AresGuiScheduler {

    private static boolean openConfigScreen;

    public static void requestConfigScreen() {
        openConfigScreen = true;
    }

    /*
     * in 1.8, the game handles all types of foreground states/UIs as
     * a singular GUI instance. trying to run the /aresconfig command
     * to open up the settings GUI on the same tick as closing the text
     * box causes the client to simultaneously close the config options GUI too.
     *
     * the hacky fix to this is to essentially "delay" opening the config GUI
     * by 1 tick, so it executes separately and won't get automatically closed.
     */
    public static void openRequestedScreen() {
        if (!openConfigScreen) {
            return;
        }

        openConfigScreen = false;
        Minecraft.getMinecraft().displayGuiScreen(new AresConfigScreen(null));
    }
}
