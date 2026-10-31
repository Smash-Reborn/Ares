package sh.smsh.ares.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModSettingsConfig {

    private ExtendedRenderDistanceSettings extendedRenderDistance = new ExtendedRenderDistanceSettings();
    private boolean disableArrowBouncingAnimation = true;
    private boolean trackDisguises = true;
    private boolean showSelfDisguiseEntity = false;
    private boolean nativeBoundingBoxes = false;
    private boolean highlightMeleeBoundingBox = false;
    private AresColor highlightMeleeBoundingBoxColor = AresColor.GOLD;
    private boolean dynamicallyScaleDisguiseBoundingBoxes = true;
    private boolean legacyDamageTilt = true;
    private boolean legacyDamageSound = true;
    private boolean disableDamageIndicators = false;
    private boolean disableAmbientDisguiseSounds = true;

    @Data
    @NoArgsConstructor
    public static class ExtendedRenderDistanceSettings {

        private boolean enabled = true;
        private float renderDistanceWeight = 12.0f;
    }
}
