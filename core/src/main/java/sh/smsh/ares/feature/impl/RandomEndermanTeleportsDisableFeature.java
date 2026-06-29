package sh.smsh.ares.feature.impl;

import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.feature.AresFeature;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.settings.ModSettings;

public final class RandomEndermanTeleportsDisableFeature implements AresFeature {

    @NotNull public static final String ID = "AresMod:DisableRandomEndermanTeleports";

    @Override
    @NotNull public String getId() {
        return ID;
    }

    public static boolean shouldDisableTeleports() {
        return FeatureManager.isActive(ID) && ModSettings.shouldDisableRandomEndermanTeleports();
    }
}
