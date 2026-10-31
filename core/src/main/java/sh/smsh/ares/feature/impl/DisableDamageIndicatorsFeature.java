package sh.smsh.ares.feature.impl;

import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.feature.AresFeature;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.settings.ModSettings;

public final class DisableDamageIndicatorsFeature implements AresFeature {

    @NotNull public static final String ID = "AresMod:DisableDamageIndicators";

    @Override
    @NotNull
    public String getId() {
        return ID;
    }

    public static boolean shouldApply() {
        return FeatureManager.isActive(ID) && ModSettings.isDisableDamageIndicators();
    }
}
