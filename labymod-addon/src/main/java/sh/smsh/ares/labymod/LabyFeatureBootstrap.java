package sh.smsh.ares.labymod;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.AresBootstrap;
import sh.smsh.ares.AresConstants;

import java.io.File;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LabyFeatureBootstrap {

    private static Path configDir;

    public static void init() {
        final File gameDir = Minecraft.getMinecraft().mcDataDir;
        configDir = new File(gameDir, AresConstants.CONFIG_DIR).toPath();
        AresBootstrap.initialize(configDir, LabyFeatureBootstrap.class.getClassLoader());
    }

    public static void reloadWhitelist() {
        if (configDir == null) {
            return;
        }

        AresBootstrap.reloadConfig();
    }
}
