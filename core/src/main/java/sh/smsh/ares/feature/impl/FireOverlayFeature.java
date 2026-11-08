package sh.smsh.ares.feature.impl;

import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.feature.AresFeature;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.settings.ModSettings;

public final class FireOverlayFeature implements AresFeature {

    @NotNull public static final String ID = "AresMod:FireOverlay";

    @Override
    @NotNull
    public String getId() {
        return ID;
    }

    public static boolean shouldApply() {
        return FeatureManager.isActive(ID) && ModSettings.isFireOverlayEnabled();
    }

    public static float getHeightOffset() {
        return ModSettings.getFireOverlayHeightOffset();
    }

    public static float getOpacity() {
        return ModSettings.getFireOverlayOpacity();
    }
}
