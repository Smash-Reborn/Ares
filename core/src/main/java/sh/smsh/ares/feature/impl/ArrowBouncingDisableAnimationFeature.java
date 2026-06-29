package sh.smsh.ares.feature.impl;

import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.feature.AresFeature;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.settings.ModSettings;

public final class ArrowBouncingDisableAnimationFeature implements AresFeature {

    @NotNull public static final String ID = "AresMod:DisableArrowBouncingAnimation";

    @Override
    @NotNull public String getId() {
        return ID;
    }

    public static boolean shouldDisableAnimation() {
        return FeatureManager.isActive(ID) && ModSettings.shouldDisableArrowBouncingAnimation();
    }
}
