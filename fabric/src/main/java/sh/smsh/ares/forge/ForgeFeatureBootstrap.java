package sh.smsh.ares.forge;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.AresBootstrap;
import sh.smsh.ares.AresConstants;
import sh.smsh.ares.feature.impl.ArrowBouncingDisableAnimationFeature;
import sh.smsh.ares.feature.impl.DisableAmbientDisguiseSoundsFeature;
import sh.smsh.ares.feature.impl.DisableDamageIndicatorsFeature;
import sh.smsh.ares.feature.impl.DisguiseDetectionFeature;
import sh.smsh.ares.feature.impl.DynamicDisguiseBoundingBoxFeature;
import sh.smsh.ares.feature.impl.ExtendedRenderDistanceFeature;
import sh.smsh.ares.feature.impl.FireOverlayFeature;
import sh.smsh.ares.feature.impl.HighlightMeleeBoundingBoxFeature;
import sh.smsh.ares.feature.impl.LegacyDamageTiltFeature;
import sh.smsh.ares.feature.impl.NativeBoundingBoxesFeature;
import sh.smsh.ares.feature.impl.ParticleOpacityFeature;
import sh.smsh.ares.feature.impl.SelfDisguiseEntityFeature;
import sh.smsh.ares.feature.FeatureManager;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ForgeFeatureBootstrap {

    public static void init(@NotNull final File gameDir) {
        AresBootstrap.initialize(
                new File(gameDir, AresConstants.CONFIG_DIR).toPath(), ForgeFeatureBootstrap.class.getClassLoader());

        // == LOAD FEATURES ==
        FeatureManager.register(new ArrowBouncingDisableAnimationFeature());
        FeatureManager.register(new ExtendedRenderDistanceFeature());
        FeatureManager.register(new DisguiseDetectionFeature());
        FeatureManager.register(new SelfDisguiseEntityFeature());
        FeatureManager.register(new NativeBoundingBoxesFeature());
        FeatureManager.register(new HighlightMeleeBoundingBoxFeature());
        FeatureManager.register(new DynamicDisguiseBoundingBoxFeature());
        FeatureManager.register(new LegacyDamageTiltFeature());
        FeatureManager.register(new DisableDamageIndicatorsFeature());
        FeatureManager.register(new DisableAmbientDisguiseSoundsFeature());
        FeatureManager.register(new FireOverlayFeature());
        FeatureManager.register(new ParticleOpacityFeature());
    }

    public static void reloadWhitelist() {
        AresBootstrap.reloadConfig();
    }
}
