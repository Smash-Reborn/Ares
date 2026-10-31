package sh.smsh.ares.fabric.gui;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AresGuiScheduler {

    private static boolean openRequested;

    public static void requestConfigScreen() {
        openRequested = true;
    }

    public static void tick() {
        if (!openRequested) {
            return;
        }

        openRequested = false;
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new AresConfigScreen(minecraft.gui.screen()));
    }
}
