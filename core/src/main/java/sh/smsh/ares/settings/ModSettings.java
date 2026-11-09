package sh.smsh.ares.settings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.AresLog;
import sh.smsh.ares.config.AresColor;
import sh.smsh.ares.config.ModSettingsConfig;
import sh.smsh.ares.config.ModSettingsLoader;

import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModSettings {

    private static final float MIN_RENDER_DISTANCE_WEIGHT = 1.0f;
    private static final float MAX_RENDER_DISTANCE_WEIGHT = 16.0f;
    public static final float MIN_FIRE_OVERLAY_OFFSET = 0.0f;
    public static final float MAX_FIRE_OVERLAY_OFFSET = 1.0f;
    public static final float MIN_VISUAL_OPACITY = 0.01f;
    public static final float MAX_VISUAL_OPACITY = 1.0f;

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
        normalizeVisualSettings();
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

    public static boolean shouldTrackDisguises() {
        return config.isTrackDisguises();
    }

    public static void setTrackDisguises(final boolean trackDisguises) {
        config.setTrackDisguises(trackDisguises);
    }

    public static boolean shouldShowSelfDisguiseEntity() {
        return config.isShowSelfDisguiseEntity();
    }

    public static void setShowSelfDisguiseEntity(final boolean showSelfDisguiseEntity) {
        config.setShowSelfDisguiseEntity(showSelfDisguiseEntity);
    }

    public static boolean isSelfDisguiseOnCrack() {
        return config.isSelfDisguiseOnCrack();
    }

    public static void setSelfDisguiseOnCrack(final boolean selfDisguiseOnCrack) {
        config.setSelfDisguiseOnCrack(selfDisguiseOnCrack);
    }

    public static boolean shouldForceNativeBoundingBoxes() {
        return config.isNativeBoundingBoxes();
    }

    public static void setForceNativeBoundingBoxes(final boolean nativeBoundingBoxes) {
        config.setNativeBoundingBoxes(nativeBoundingBoxes);
    }

    public static boolean shouldHighlightMeleeBoundingBox() {
        return config.isHighlightMeleeBoundingBox();
    }

    public static void setHighlightMeleeBoundingBox(final boolean highlightMeleeBoundingBox) {
        config.setHighlightMeleeBoundingBox(highlightMeleeBoundingBox);
    }

    @NotNull
    public static AresColor getHighlightMeleeBoundingBoxColor() {
        return AresColor.orDefault(config.getHighlightMeleeBoundingBoxColor(), AresColor.GOLD);
    }

    public static void setHighlightMeleeBoundingBoxColor(@NotNull final AresColor color) {
        config.setHighlightMeleeBoundingBoxColor(color);
    }

    public static boolean shouldDynamicallyScaleDisguiseBoundingBoxes() {
        return config.isDynamicallyScaleDisguiseBoundingBoxes();
    }

    public static void setDynamicallyScaleDisguiseBoundingBoxes(final boolean enabled) {
        config.setDynamicallyScaleDisguiseBoundingBoxes(enabled);
    }

    public static boolean isLegacyDamageTilt() {
        return config.isLegacyDamageTilt();
    }

    public static void setLegacyDamageTilt(final boolean legacyDamageTilt) {
        config.setLegacyDamageTilt(legacyDamageTilt);
    }

    public static boolean isDisableDamageIndicators() {
        return config.isDisableDamageIndicators();
    }

    public static void setDisableDamageIndicators(final boolean disableDamageIndicators) {
        config.setDisableDamageIndicators(disableDamageIndicators);
    }

    public static boolean isDisableAmbientDisguiseSounds() {
        return config.isDisableAmbientDisguiseSounds();
    }

    public static void setDisableAmbientDisguiseSounds(final boolean disableAmbientDisguiseSounds) {
        config.setDisableAmbientDisguiseSounds(disableAmbientDisguiseSounds);
    }

    public static boolean isFireOverlayEnabled() {
        return fireOverlay().isEnabled();
    }

    public static void setFireOverlayEnabled(final boolean enabled) {
        fireOverlay().setEnabled(enabled);
    }

    public static float getFireOverlayHeightOffset() {
        return clampFireOverlayOffset(fireOverlay().getOverlayHeightOffset());
    }

    public static void setFireOverlayHeightOffset(final float overlayHeightOffset) {
        fireOverlay().setOverlayHeightOffset(clampFireOverlayOffset(overlayHeightOffset));
    }

    public static float getFireOverlayOpacity() {
        return clampVisualOpacity(fireOverlay().getOverlayOpacity());
    }

    public static void setFireOverlayOpacity(final float overlayOpacity) {
        fireOverlay().setOverlayOpacity(clampVisualOpacity(overlayOpacity));
    }

    public static boolean isParticleOpacityEnabled() {
        return particleOpacity().isEnabled();
    }

    public static void setParticleOpacityEnabled(final boolean enabled) {
        particleOpacity().setEnabled(enabled);
    }

    public static float getParticleOpacity() {
        return clampVisualOpacity(particleOpacity().getParticleOpacity());
    }

    public static void setParticleOpacity(final float particleOpacity) {
        particleOpacity().setParticleOpacity(clampVisualOpacity(particleOpacity));
    }

    public static boolean isDisguiseGlowEnabled() {
        return disguiseGlow().isEnabled();
    }

    public static void setDisguiseGlowEnabled(final boolean enabled) {
        disguiseGlow().setEnabled(enabled);
    }

    @NotNull
    public static ModSettingsConfig.DisguiseGlowSettings.DisguiseGlowToggleType getDisguiseGlowToggleType() {
        return disguiseGlow().getGlowToggleType();
    }

    public static void setDisguiseGlowToggleType(
            @NotNull final ModSettingsConfig.DisguiseGlowSettings.DisguiseGlowToggleType glowToggleType) {
        disguiseGlow().setGlowToggleType(glowToggleType);
    }

    public static void cycleDisguiseGlowToggleType() {
        disguiseGlow().setGlowToggleType(getDisguiseGlowToggleType().next());
    }

    public static boolean useCustomDisguiseStockColors() {
        return disguiseGlow().isUseCustomStockColors();
    }

    public static void setUseCustomDisguiseStockColors(final boolean useCustomStockColors) {
        disguiseGlow().setUseCustomStockColors(useCustomStockColors);
    }

    @NotNull
    public static AresColor getDisguiseGlowFourStocksColor() {
        return disguiseGlow().getFourStocksAndAbove();
    }

    public static void setDisguiseGlowFourStocksColor(@NotNull final AresColor color) {
        disguiseGlow().setFourStocksAndAbove(color);
    }

    @NotNull
    public static AresColor getDisguiseGlowThreeStocksColor() {
        return disguiseGlow().getThreeStocks();
    }

    public static void setDisguiseGlowThreeStocksColor(@NotNull final AresColor color) {
        disguiseGlow().setThreeStocks(color);
    }

    @NotNull
    public static AresColor getDisguiseGlowTwoStocksColor() {
        return disguiseGlow().getTwoStocks();
    }

    public static void setDisguiseGlowTwoStocksColor(@NotNull final AresColor color) {
        disguiseGlow().setTwoStocks(color);
    }

    @NotNull
    public static AresColor getDisguiseGlowOneStockColor() {
        return disguiseGlow().getOneStock();
    }

    public static void setDisguiseGlowOneStockColor(@NotNull final AresColor color) {
        disguiseGlow().setOneStock(color);
    }

    @NotNull
    public static AresColor getDisguiseGlowFallbackColor() {
        return disguiseGlow().getDefaultFallback();
    }

    public static void setDisguiseGlowFallbackColor(@NotNull final AresColor color) {
        disguiseGlow().setDefaultFallback(color);
    }

    @NotNull
    public static AresColor getDisguiseGlowColor(@Nullable final Integer stocks) {
        return disguiseGlow().colorForStock(stocks);
    }

    public static int getDisguiseGlowToggleKeyCode() {
        final int code = disguiseGlow().getToggleKeyCode();
        return code > 0 ? code : -1;
    }

    public static void setDisguiseGlowToggleKeyCode(final int toggleKeyCode) {
        disguiseGlow().setToggleKeyCode(toggleKeyCode > 0 ? toggleKeyCode : -1);
    }

    public static boolean saveConfig() {
        if (loader == null) {
            return false;
        }

        normalizeVisualSettings();

        try {
            loader.save(config);
            return true;
        } catch (Exception ex) {
            AresLog.LOGGER.error("Failed to save mod settings.", ex);
            return false;
        }
    }

    private static void normalizeVisualSettings() {
        setFireOverlayHeightOffset(getFireOverlayHeightOffset());
        setFireOverlayOpacity(getFireOverlayOpacity());
        setParticleOpacity(getParticleOpacity());
    }

    @NotNull
    private static ModSettingsConfig.FireOverlaySettings fireOverlay() {
        ModSettingsConfig.FireOverlaySettings settings = config.getFireOverlaySettings();
        if (settings == null) {
            settings = new ModSettingsConfig.FireOverlaySettings();
            config.setFireOverlaySettings(settings);
        }
        return settings;
    }

    @NotNull
    private static ModSettingsConfig.ParticleOpacitySettings particleOpacity() {
        ModSettingsConfig.ParticleOpacitySettings settings = config.getParticleOpacitySettings();
        if (settings == null) {
            settings = new ModSettingsConfig.ParticleOpacitySettings();
            config.setParticleOpacitySettings(settings);
        }
        return settings;
    }

    @NotNull
    private static ModSettingsConfig.DisguiseGlowSettings disguiseGlow() {
        ModSettingsConfig.DisguiseGlowSettings settings = config.getDisguiseGlowSettings();
        if (settings == null) {
            settings = new ModSettingsConfig.DisguiseGlowSettings();
            config.setDisguiseGlowSettings(settings);
        }
        if (settings.getGlowToggleType() == null) {
            settings.setGlowToggleType(ModSettingsConfig.DisguiseGlowSettings.DisguiseGlowToggleType.PERMANENTLY_ACTIVE);
        }
        if (settings.getFourStocksAndAbove() == null) {
            settings.setFourStocksAndAbove(AresColor.GREEN);
        }
        if (settings.getThreeStocks() == null) {
            settings.setThreeStocks(AresColor.YELLOW);
        }
        if (settings.getTwoStocks() == null) {
            settings.setTwoStocks(AresColor.GOLD);
        }
        if (settings.getOneStock() == null) {
            settings.setOneStock(AresColor.RED);
        }
        if (settings.getDefaultFallback() == null) {
            settings.setDefaultFallback(AresColor.WHITE);
        }
        return settings;
    }

    @ApiStatus.Internal
    private static float clampRenderDistanceWeight(final float renderDistanceWeight) {
        return Math.max(MIN_RENDER_DISTANCE_WEIGHT, Math.min(MAX_RENDER_DISTANCE_WEIGHT, renderDistanceWeight));
    }

    private static float clampFireOverlayOffset(final float overlayHeightOffset) {
        if (Float.isNaN(overlayHeightOffset)) {
            return MIN_FIRE_OVERLAY_OFFSET;
        }
        return Math.max(MIN_FIRE_OVERLAY_OFFSET, Math.min(MAX_FIRE_OVERLAY_OFFSET, overlayHeightOffset));
    }

    private static float clampVisualOpacity(final float opacity) {
        if (Float.isNaN(opacity)) {
            return MAX_VISUAL_OPACITY;
        }
        return Math.max(MIN_VISUAL_OPACITY, Math.min(MAX_VISUAL_OPACITY, opacity));
    }
}
