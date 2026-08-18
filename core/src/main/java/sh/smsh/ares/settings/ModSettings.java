package sh.smsh.ares.settings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.AresLog;
import sh.smsh.ares.config.AresColor;
import sh.smsh.ares.config.ModSettingsConfig;
import sh.smsh.ares.config.ModSettingsLoader;
import sh.smsh.ares.disguise.DisguiseRegistry;

import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModSettings {

    private static final float MIN_RENDER_DISTANCE_WEIGHT = 1.0f;
    private static final float MAX_RENDER_DISTANCE_WEIGHT = 16.0f;

    private static ModSettingsLoader loader;
    private static ModSettingsConfig config = new ModSettingsConfig();

    public static void initialize(@NotNull final Path configPath, @NotNull final ModSettingsConfig defaultConfig) {
        loader = new ModSettingsLoader(configPath, defaultConfig);
        reloadConfig();
    }

    public static void reloadConfig() {
        if (loader == null) {
            return;
        }

        try {
            config = loader.load();
        } catch (Exception ex) {
            config = new ModSettingsConfig();
        }
    }

    public static boolean isExtendedRenderDistanceEnabled() {
        return config.getExtendedRenderDistance().isEnabled();
    }

    public static void setExtendedRenderDistanceEnabled(final boolean enabled) {
        config.getExtendedRenderDistance().setEnabled(enabled);
    }

    public static float getRenderDistanceWeight() {
        return clampRenderDistanceWeight(config.getExtendedRenderDistance().getRenderDistanceWeight());
    }

    public static void setRenderDistanceWeight(final float renderDistanceWeight) {
        config.getExtendedRenderDistance().setRenderDistanceWeight(clampRenderDistanceWeight(renderDistanceWeight));
    }

    public static boolean shouldDisableArrowBouncingAnimation() {
        return config.isDisableArrowBouncingAnimation();
    }

    public static void setDisableArrowBouncingAnimation(final boolean disableArrowBouncingAnimation) {
        config.setDisableArrowBouncingAnimation(disableArrowBouncingAnimation);
    }

    public static boolean shouldDisableRandomEndermanTeleports() {
        return config.isDisableRandomEndermanTeleports();
    }

    public static void setDisableRandomEndermanTeleports(final boolean disableRandomEndermanTeleports) {
        config.setDisableRandomEndermanTeleports(disableRandomEndermanTeleports);
    }

    public static boolean shouldTrackDisguises() {
        return config.isTrackDisguises();
    }

    public static void setTrackDisguises(final boolean trackDisguises) {
        final boolean wasTracking = config.isTrackDisguises();
        config.setTrackDisguises(trackDisguises);
        if (wasTracking && !trackDisguises) {
            DisguiseRegistry.clear();
        }
    }

    public static boolean shouldShowSelfDisguiseEntity() {
        return config.isShowSelfDisguiseEntity();
    }

    public static void setShowSelfDisguiseEntity(final boolean showSelfDisguiseEntity) {
        config.setShowSelfDisguiseEntity(showSelfDisguiseEntity);
    }

    public static boolean shouldUseNativeNametag() {
        return config.isUseNativeNametag();
    }

    public static void setUseNativeNametag(final boolean useNativeNametag) {
        config.setUseNativeNametag(useNativeNametag);
    }

    @NotNull
    public static AresColor getDisguiseNametagColor() {
        return AresColor.orDefault(config.getDisguiseNametagColor(), AresColor.YELLOW);
    }

    public static void setDisguiseNametagColor(@NotNull final AresColor disguiseNametagColor) {
        config.setDisguiseNametagColor(disguiseNametagColor);
    }

    public static boolean shouldShowHittableNametagBoundingBox() {
        return config.isShowHittableNametagBoundingBox();
    }

    public static void setShowHittableNametagBoundingBox(final boolean showHittableNametagBoundingBox) {
        config.setShowHittableNametagBoundingBox(showHittableNametagBoundingBox);
    }

    @NotNull
    public static AresColor getHittableNametagBoundingBoxColor() {
        return AresColor.orDefault(config.getHittableNametagBoundingBoxColor(), AresColor.BLUE);
    }

    public static void setHittableNametagBoundingBoxColor(@NotNull final AresColor hittableNametagBoundingBoxColor) {
        config.setHittableNametagBoundingBoxColor(hittableNametagBoundingBoxColor);
    }

    public static boolean shouldShowServersideBoundingBox() {
        return config.isShowServersideBoundingBox();
    }

    public static void setShowServersideBoundingBox(final boolean showServersideBoundingBox) {
        config.setShowServersideBoundingBox(showServersideBoundingBox);
    }

    @NotNull
    public static AresColor getServersideBoundingBoxColor() {
        return AresColor.orDefault(config.getServersideBoundingBoxColor(), AresColor.GREEN);
    }

    public static void setServersideBoundingBoxColor(@NotNull final AresColor serversideBoundingBoxColor) {
        config.setServersideBoundingBoxColor(serversideBoundingBoxColor);
    }

    public static boolean saveConfig() {
        if (loader == null) {
            return false;
        }

        try {
            loader.save(config);
            return true;
        } catch (Exception ex) {
            AresLog.LOGGER.error("Failed to save mod settings.", ex);
            return false;
        }
    }

    @ApiStatus.Internal
    private static float clampRenderDistanceWeight(final float renderDistanceWeight) {
        return Math.max(MIN_RENDER_DISTANCE_WEIGHT, Math.min(MAX_RENDER_DISTANCE_WEIGHT, renderDistanceWeight));
    }
}
