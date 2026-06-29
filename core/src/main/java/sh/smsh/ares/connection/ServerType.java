package sh.smsh.ares.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ServerType {

    // == DEFAULT ==
    UNKNOWN("NONE"),

    // include all mineplex domains
    MINEPLEX("MINEPLEX",
            "mineplex.com",
            "us.mineplex.com",
            "play.mineplex.com"),

    SSM_REBORN_LEGACY("SSM REBORN (Legacy)",
            "us.smashreborn.com",
            "eu.smashreborn.com",
            "au.smashreborn.com",
            "51.161.76.173:25596",      // direct US
            "194.164.96.8:25578",       // direct EU
            "139.99.136.114:25569"),    // direct AU

    // SSM_REBORN_OS(),
    // SSM_OS()
    ;

    @Getter @NotNull private final String displayName;
    @Getter @NotNull private final List<String> domains;

    ServerType(@NotNull final String displayName, @NotNull final String... domains) {
        this.displayName = displayName;
        this.domains = Collections.unmodifiableList(Arrays.asList(domains));
    }

    public boolean matches(@Nullable final String rawAddress) {
        if (this == UNKNOWN || rawAddress == null) {
            return false;
        }

        for (final String domain : domains) {
            if (ServerAddress.matches(rawAddress, domain, true)) {
                return true;
            }
        }

        return false;
    }

    @NotNull public static ServerType fromAddress(@Nullable final String rawAddress) {
        if (rawAddress == null) {
            return UNKNOWN;
        }

        for (final ServerType serverType : values()) {
            if (serverType.matches(rawAddress)) {
                return serverType;
            }
        }

        return UNKNOWN;
    }
}
