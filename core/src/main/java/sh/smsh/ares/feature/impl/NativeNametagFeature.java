package sh.smsh.ares.feature.impl;

import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.config.AresColor;
import sh.smsh.ares.feature.AresFeature;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.settings.ModSettings;

public final class NativeNametagFeature implements AresFeature {

    @NotNull public static final String ID = "AresMod:NativeNametag";

    @Override
    @NotNull public String getId() {
        return ID;
    }

    public static boolean shouldApply() {
        return FeatureManager.isActive(ID) && ModSettings.shouldUseNativeNametag();
    }

    @NotNull
    public static AresColor getNametagColor() {
        return ModSettings.getDisguiseNametagColor();
    }
}
