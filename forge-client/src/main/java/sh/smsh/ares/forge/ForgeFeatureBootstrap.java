package sh.smsh.ares.forge;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.AresBootstrap;
import sh.smsh.ares.AresConstants;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ForgeFeatureBootstrap {

    public static void init(@NotNull final File gameDir) {
        AresBootstrap.initialize(
                new File(gameDir, AresConstants.CONFIG_DIR + "/" + AresConstants.SERVERS_CONFIG_FILE).toPath(),
                "default_servers.json", ForgeFeatureBootstrap.class.getClassLoader());
    }

    public static void reloadWhitelist() {
        AresBootstrap.reloadConfig();
    }
}