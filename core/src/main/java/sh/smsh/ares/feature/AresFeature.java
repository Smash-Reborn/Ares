package sh.smsh.ares.feature;

import org.jetbrains.annotations.NotNull;

public interface AresFeature {

    @NotNull String getId();

    default void onEnable() { }

    default void onDisable() { }

    default void onClientTick() { }
}