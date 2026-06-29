package sh.smsh.ares.gate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.config.ServerWhitelistConfig;
import sh.smsh.ares.config.ServerWhitelistLoader;
import sh.smsh.ares.connection.ServerAddress;
import sh.smsh.ares.connection.ServerType;

import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerGate {

    private static ServerWhitelistLoader loader;
    private static ServerWhitelistConfig config = new ServerWhitelistConfig();

    @Getter private static String currentServer;
    @Getter @NotNull private static ServerType currentServerType = ServerType.UNKNOWN;

    public static void initialize(@NotNull final Path configPath, @NotNull final ServerWhitelistConfig defaultConfig) {
        loader = new ServerWhitelistLoader(configPath, defaultConfig);
        reloadConfig();
    }

    public static void reloadConfig() {
        if (loader == null) {
            return;
        }

        try {
            config = loader.load();
        } catch (Exception ex) {
            config = new ServerWhitelistConfig();
        }
    }

    public static void updateCurrentServer(@Nullable final String rawAddress) {
        currentServer = rawAddress != null ? ServerAddress.normalize(rawAddress) : null;
        currentServerType = ServerType.fromAddress(rawAddress);
    }

    public static void clearCurrentServer() {
        currentServer = null;
        currentServerType = ServerType.UNKNOWN;
    }

    public static boolean isAllowed() {
        if (loader == null) {
            return false;
        }

        if (currentServer == null) {
            return false;
        }

        if (!config.isEnabled()) {
            return false;
        }

        for (final String pattern : config.getServers()) {
            if (matches(currentServer, pattern)) {
                return true;
            }
        }

        return false;
    }

    public static String normalizeAddress(@NotNull final String raw) {
        return ServerAddress.normalize(raw);
    }

    public static boolean matches(@NotNull final String host, @NotNull final String pattern) {
        return ServerAddress.matches(host, pattern, config.isAllowSubdomains());
    }
}