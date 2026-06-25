package sh.smsh.ares;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.config.ServerWhitelistConfig;
import sh.smsh.ares.gate.ServerGate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AresBootstrap {

    @NotNull private static final Gson GSON = new Gson();

    public static void initialize(@NotNull final Path configPath, @NotNull final String defaultConfigResource, @NotNull final ClassLoader classLoader) {
        ServerGate.initialize(configPath, loadDefaultConfig(defaultConfigResource, classLoader));
    }

    public static void reloadConfig() {
        ServerGate.reloadConfig();
    }

    @ApiStatus.Internal
    @NotNull private static ServerWhitelistConfig loadDefaultConfig(@NotNull final String resourcePath, @NotNull final ClassLoader classLoader) {
        final InputStream stream = classLoader.getResourceAsStream(resourcePath);

        if (stream == null) {
            return new ServerWhitelistConfig();
        }

        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            ServerWhitelistConfig config = GSON.fromJson(reader, ServerWhitelistConfig.class);
            return config != null ? config : new ServerWhitelistConfig();
        } catch (Exception ex) {
            return new ServerWhitelistConfig();
        }
    }
}