package sh.smsh.ares.feature.impl;

import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.feature.AresFeature;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.settings.ModSettings;

public final class DisguiseDetectionFeature implements AresFeature {

    @NotNull public static final String ID = "AresMod:DisguiseDetection";

    @Override
    @NotNull public String getId() {
        return ID;
    }

    public static boolean shouldApply() {
        return FeatureManager.isActive(ID) && ModSettings.shouldTrackDisguises();
    }
}
