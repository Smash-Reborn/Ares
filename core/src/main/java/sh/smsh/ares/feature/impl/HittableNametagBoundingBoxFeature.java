package sh.smsh.ares.feature.impl;

import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.config.AresColor;
import sh.smsh.ares.feature.AresFeature;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.settings.ModSettings;

public final class HittableNametagBoundingBoxFeature implements AresFeature {

    @NotNull public static final String ID = "AresMod:HittableNametagBoundingBox";

    @Override
    @NotNull public String getId() {
        return ID;
    }

    public static boolean shouldApply() {
        return FeatureManager.isActive(ID) && ModSettings.shouldShowHittableNametagBoundingBox();
    }

    @NotNull
    public static AresColor getColor() {
        return ModSettings.getHittableNametagBoundingBoxColor();
    }
}
