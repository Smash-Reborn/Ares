package sh.smsh.ares.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerAddress {

    @NotNull public static String normalize(@NotNull final String raw) {
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

    public static boolean matches(
            @NotNull final String host,
            @NotNull final String pattern,
            final boolean allowSubdomains) {

        final String normalizedHost = normalize(host);
        final String normalizedPattern = normalize(pattern);

        if (normalizedHost.isEmpty() || normalizedPattern.isEmpty()) {
            return false;
        }

        if (normalizedHost.equals(normalizedPattern)) {
            return true;
        }

        if (normalizedPattern.startsWith("*.")) {
            final String suffix = normalizedPattern.substring(2);
            return allowSubdomains
                    && (normalizedHost.equals(suffix) || normalizedHost.endsWith("." + suffix));
        }

        return allowSubdomains && normalizedHost.endsWith("." + normalizedPattern);
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
