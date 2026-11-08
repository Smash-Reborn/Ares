package sh.smsh.ares.feature.impl;

import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.feature.AresFeature;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.settings.ModSettings;

public final class ParticleOpacityFeature implements AresFeature {

    @NotNull public static final String ID = "AresMod:ParticleOpacity";

    @Override
    @NotNull
    public String getId() {
        return ID;
    }

    public static boolean shouldApply() {
        return FeatureManager.isActive(ID) && ModSettings.isParticleOpacityEnabled();
    }

    public static float getOpacity() {
        return ModSettings.getParticleOpacity();
    }
}
