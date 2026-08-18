package sh.smsh.ares.feature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sh.smsh.ares.feature.impl.ArrowBouncingDisableAnimationFeature;
import sh.smsh.ares.feature.impl.DisguiseDetectionFeature;
import sh.smsh.ares.feature.impl.ExtendedRenderDistanceFeature;
import sh.smsh.ares.feature.impl.HittableNametagBoundingBoxFeature;
import sh.smsh.ares.feature.impl.NativeNametagFeature;
import sh.smsh.ares.feature.impl.RandomEndermanTeleportsDisableFeature;
import sh.smsh.ares.feature.impl.SelfDisguiseEntityFeature;
import sh.smsh.ares.feature.impl.ServersideBoundingBoxFeature;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeatureBootstrap {

    public static void registerAll() {
        FeatureManager.register(new ExtendedRenderDistanceFeature());
        FeatureManager.register(new ArrowBouncingDisableAnimationFeature());
        FeatureManager.register(new RandomEndermanTeleportsDisableFeature());
        FeatureManager.register(new DisguiseDetectionFeature());
        FeatureManager.register(new SelfDisguiseEntityFeature());
        FeatureManager.register(new NativeNametagFeature());
        FeatureManager.register(new HittableNametagBoundingBoxFeature());
        FeatureManager.register(new ServersideBoundingBoxFeature());
    }
}
