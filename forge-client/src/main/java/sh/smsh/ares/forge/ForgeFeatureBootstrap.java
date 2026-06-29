package sh.smsh.ares.forge;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.AresBootstrap;
import sh.smsh.ares.AresConstants;
import sh.smsh.ares.feature.impl.ArrowBouncingDisableAnimationFeature;
import sh.smsh.ares.feature.impl.RandomEndermanTeleportsDisableFeature;
import sh.smsh.ares.feature.impl.ExtendedRenderDistanceFeature;
import sh.smsh.ares.feature.FeatureManager;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ForgeFeatureBootstrap {

    public static void init(@NotNull final File gameDir) {
        AresBootstrap.initialize(
                new File(gameDir, AresConstants.CONFIG_DIR).toPath(), ForgeFeatureBootstrap.class.getClassLoader());

        // == LOAD FEATURES ==
        FeatureManager.register(new ArrowBouncingDisableAnimationFeature());
        FeatureManager.register(new RandomEndermanTeleportsDisableFeature());
        FeatureManager.register(new ExtendedRenderDistanceFeature());
    }

    public static void reloadWhitelist() {
        AresBootstrap.reloadConfig();
    }
}
