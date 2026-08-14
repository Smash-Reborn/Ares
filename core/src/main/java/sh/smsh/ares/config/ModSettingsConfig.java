package sh.smsh.ares.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModSettingsConfig {

    private ExtendedRenderDistanceSettings extendedRenderDistance = new ExtendedRenderDistanceSettings();
    private boolean disableArrowBouncingAnimation = true;
    private boolean disableRandomEndermanTeleports = true;
    private boolean trackDisguises = true;
    private boolean useNativeNametag = true;
    private boolean showHittableNametagBoundingBox = true;
    private boolean showServersideBoundingBox = false;

    @Data
    @NoArgsConstructor
    public static class ExtendedRenderDistanceSettings {

        private boolean enabled = true;
        private float renderDistanceWeight = 12.0f;
    }
}
