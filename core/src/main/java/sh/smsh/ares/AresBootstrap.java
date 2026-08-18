package sh.smsh.ares;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.config.ModSettingsConfig;
import sh.smsh.ares.config.ServerWhitelistConfig;
import sh.smsh.ares.feature.FeatureBootstrap;
import sh.smsh.ares.gate.ServerGate;
import sh.smsh.ares.settings.ModSettings;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AresBootstrap {

    @NotNull private static final Gson GSON = new Gson();

    public static void initialize(@NotNull final Path configDir, @NotNull final ClassLoader classLoader) {
        ServerGate.initialize(
                configDir.resolve(AresConstants.SERVERS_CONFIG_FILE),
                loadDefaultConfig("default_servers.json", ServerWhitelistConfig.class, classLoader));
        ModSettings.initialize(
                configDir.resolve(AresConstants.SETTINGS_CONFIG_FILE),
                loadDefaultConfig("default_settings.json", ModSettingsConfig.class, classLoader));
        FeatureBootstrap.registerAll();
    }

    public static void reloadConfig() {
        ServerGate.reloadConfig();
        ModSettings.reloadConfig();
    }

    @ApiStatus.Internal
    @NotNull private static <T> T loadDefaultConfig(
            @NotNull final String resourcePath,
            @NotNull final Class<T> type,
            @NotNull final ClassLoader classLoader) {

        final InputStream stream = classLoader.getResourceAsStream(resourcePath);

        if (stream == null) {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to create default config for " + type.getSimpleName(), ex);
            }
        }

        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            final T config = GSON.fromJson(reader, type);

            if (config != null) {
                return config;
            }
        } catch (Exception ignored) { }

        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create default config for " + type.getSimpleName(), ex);
        }
    }
}
