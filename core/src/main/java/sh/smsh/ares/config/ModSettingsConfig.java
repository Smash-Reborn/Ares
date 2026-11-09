package sh.smsh.ares.config;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
public class ModSettingsConfig {

    private ExtendedRenderDistanceSettings extendedRenderDistance = new ExtendedRenderDistanceSettings();
    private boolean disableArrowBouncingAnimation = true;
    private boolean trackDisguises = true;
    private boolean showSelfDisguiseEntity = false;
    private boolean selfDisguiseOnCrack = false;
    private boolean nativeBoundingBoxes = false;
    private boolean highlightMeleeBoundingBox = false;
    private AresColor highlightMeleeBoundingBoxColor = AresColor.GOLD;
    private boolean dynamicallyScaleDisguiseBoundingBoxes = true;
    private boolean legacyDamageTilt = true;
    private boolean disableDamageIndicators = false;
    private boolean disableAmbientDisguiseSounds = true;
    private FireOverlaySettings fireOverlaySettings = new FireOverlaySettings();
    private ParticleOpacitySettings particleOpacitySettings = new ParticleOpacitySettings();
    private DisguiseGlowSettings disguiseGlowSettings = new DisguiseGlowSettings();

    @Data
    @NoArgsConstructor
    public static class ExtendedRenderDistanceSettings {

        private boolean enabled = true;
        private float renderDistanceWeight = 12.0f;
    }

    @Data
    @NoArgsConstructor
    public static class FireOverlaySettings {

        private boolean enabled = false;
        private float overlayHeightOffset = 0.0f;
        private float overlayOpacity = 1.0f;
    }

    @Data
    @NoArgsConstructor
    public static class ParticleOpacitySettings {

        private boolean enabled = false;
        private float particleOpacity = 1.0f;
    }

    @Data
    @NoArgsConstructor
    public static class DisguiseGlowSettings {

        private boolean enabled = false;
        private DisguiseGlowToggleType glowToggleType = DisguiseGlowToggleType.PERMANENTLY_ACTIVE;
        private boolean useCustomStockColors = false;
        private AresColor fourStocksAndAbove = AresColor.GREEN;
        private AresColor threeStocks = AresColor.YELLOW;
        private AresColor twoStocks = AresColor.GOLD;
        private AresColor oneStock = AresColor.RED;
        private AresColor defaultFallback = AresColor.WHITE;
        /** GLFW keyboard key that toggles glow. {@code -1} is unbound. */
        private int toggleKeyCode = -1;

        @NotNull
        public AresColor colorForStock(@Nullable final Integer stocks) {
            if (!this.useCustomStockColors || stocks == null || stocks < 1) {
                return AresColor.orDefault(this.defaultFallback, AresColor.WHITE);
            }
            if (stocks >= 4) {
                return AresColor.orDefault(this.fourStocksAndAbove, AresColor.GREEN);
            }
            if (stocks == 3) {
                return AresColor.orDefault(this.threeStocks, AresColor.YELLOW);
            }
            if (stocks == 2) {
                return AresColor.orDefault(this.twoStocks, AresColor.GOLD);
            }
            return AresColor.orDefault(this.oneStock, AresColor.RED);
        }

        @Getter
        @RequiredArgsConstructor
        public enum DisguiseGlowToggleType {

            PERMANENTLY_ACTIVE("permanent", "Permanently Active"),
            ACTIVE_ONLY_WHEN_HOLDING_COMPASS("holding_compass", "Holding Compass"),
            TOGGLE_VIA_COMPASS_CLICK("toggle_compass", "Compass Click"),
            TOGGLE_VIA_BUTTON_BINDED_PRESS("toggle_button", "Keybind");

            @NotNull private final String configurationFieldName;
            @NotNull private final String label;

            @NotNull
            public DisguiseGlowToggleType next() {
                final DisguiseGlowToggleType[] values = values();
                return values[(this.ordinal() + 1) % values.length];
            }
        }
    }
}
