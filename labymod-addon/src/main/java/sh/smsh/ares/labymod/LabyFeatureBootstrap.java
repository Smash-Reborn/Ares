package sh.smsh.ares.labymod;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import sh.smsh.ares.AresBootstrap;
import sh.smsh.ares.AresConstants;

import java.io.File;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LabyFeatureBootstrap {

    private static Path configPath;

    public static void init() {
        final File gameDir = Minecraft.getMinecraft().mcDataDir;
        configPath = new File(gameDir, AresConstants.CONFIG_DIR + "/" + AresConstants.SERVERS_CONFIG_FILE).toPath();
        AresBootstrap.initialize(configPath, "default_servers.json", LabyFeatureBootstrap.class.getClassLoader());
    }

    public static void reloadWhitelist() {
        if (configPath == null) {
            return;
        }

        AresBootstrap.reloadConfig();
    }
}