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
    private AresColor disguiseNametagColor = AresColor.YELLOW;
    private boolean showHittableNametagBoundingBox = true;
    private AresColor hittableNametagBoundingBoxColor = AresColor.BLUE;
    private boolean showServersideBoundingBox = false;
    private AresColor serversideBoundingBoxColor = AresColor.GREEN;

    @Data
    @NoArgsConstructor
    public static class ExtendedRenderDistanceSettings {

        private boolean enabled = true;
        private float renderDistanceWeight = 12.0f;
    }
}
