package sh.smsh.ares.gate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.config.ServerWhitelistConfig;
import sh.smsh.ares.config.ServerWhitelistLoader;

import java.nio.file.Path;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerGate {

    private static ServerWhitelistLoader loader;
    private static ServerWhitelistConfig config = new ServerWhitelistConfig();

    @Getter private static String currentServer;

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
        currentServer = rawAddress != null ? normalizeAddress(rawAddress) : null;
    }

    public static void clearCurrentServer() {
        currentServer = null;
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
        final String trimmed = raw.trim().toLowerCase(Locale.ROOT);

        if (trimmed.isEmpty()) {
            return trimmed;
        }

        String withoutPort;

        if (trimmed.startsWith("[")) {
            int end = trimmed.indexOf(']');
            withoutPort = end >= 0 ? trimmed.substring(0, end + 1) : trimmed;
        } else {
            int colonIndex = trimmed.lastIndexOf(':');
            if (colonIndex > 0 && isDigitsOnly(trimmed.substring(colonIndex + 1))) {
                withoutPort = trimmed.substring(0, colonIndex);
            } else {
                withoutPort = trimmed;
            }
        }

        if (withoutPort.startsWith("[")) {
            withoutPort = withoutPort.substring(1);
        }

        if (withoutPort.endsWith("]")) {
            withoutPort = withoutPort.substring(0, withoutPort.length() - 1);
        }

        return withoutPort;
    }

    public static boolean matches(@NotNull final String host, @NotNull final String pattern) {
        final String normalizedHost = normalizeAddress(host);
        final String normalizedPattern = normalizeAddress(pattern);

        if (normalizedHost.isEmpty() || normalizedPattern.isEmpty()) {
            return false;
        }

        if (normalizedHost.equals(normalizedPattern)) {
            return true;
        }

        if (normalizedPattern.startsWith("*.")) {
            final String suffix = normalizedPattern.substring(2);
            return config.isAllowSubdomains()
                && (normalizedHost.equals(suffix) || normalizedHost.endsWith("." + suffix));
        }

        return config.isAllowSubdomains() && normalizedHost.endsWith("." + normalizedPattern);
    }

    @ApiStatus.Internal
    private static boolean isDigitsOnly(@NotNull final String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }

        return !value.isEmpty();
    }
}