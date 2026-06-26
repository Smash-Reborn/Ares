package sh.smsh.ares.settings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.config.ModSettingsConfig;
import sh.smsh.ares.config.ModSettingsLoader;

import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModSettings {

    private static ModSettingsLoader loader;
    private static ModSettingsConfig config = new ModSettingsConfig();

    public static void initialize(@NotNull final Path configPath, @NotNull final ModSettingsConfig defaultConfig) {
        loader = new ModSettingsLoader(configPath, defaultConfig);
        reloadConfig();
    }

    public static void reloadConfig() {
        if (loader == null) {
            return;
        }

        try {
            config = loader.load();
        } catch (Exception ex) {
            config = new ModSettingsConfig();
        }
    }

    public static boolean isExtendedRenderDistanceEnabled() {
        return config.getExtendedRenderDistance().isEnabled();
    }

    public static float getRenderDistanceWeight() {
        return config.getExtendedRenderDistance().getRenderDistanceWeight();
    }
}
