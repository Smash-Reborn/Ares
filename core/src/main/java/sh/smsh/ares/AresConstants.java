package sh.smsh.ares;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AresConstants {

    // keep these in sync with gradle.properties
    @NotNull public static final String MOD_ID = "aresmod";
    @NotNull public static final String MOD_NAME = "Ares";
    @NotNull public static final String VERSION = "2.1.000";
    @NotNull public static final String CONFIG_DIR = "config/AresMod";
    @NotNull public static final String SERVERS_CONFIG_FILE = "servers.json";
    @NotNull public static final String SETTINGS_CONFIG_FILE = "settings.json";

}