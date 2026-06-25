package sh.smsh.ares.feature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.gate.ServerGate;

import java.util.LinkedHashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeatureManager {

    @NotNull private static final Set<AresFeature> features = new LinkedHashSet<>();

    private static boolean active = false;

    public static void register(@NotNull final AresFeature feature) {
        features.add(feature);

        if (active && ServerGate.isAllowed()) {
            feature.onEnable();
        }
    }

    public static void evaluateGate() {
        final boolean allowed = ServerGate.isAllowed();

        if (allowed == active) {
            return;
        }

        active = allowed;

        for (final AresFeature feature : features) {
            if (active) {
                feature.onEnable();
            } else {
                feature.onDisable();
            }
        }
    }

    public static void disableAll() {
        if (!active) {
            return;
        }

        active = false;
        for (final AresFeature feature : features) {
            feature.onDisable();
        }
    }

    public static void onClientTick() {
        if (!active) {
            return;
        }

        for (final AresFeature feature : features) {
            feature.onClientTick();
        }
    }

    public static void clear() {
        disableAll();
        features.clear();
    }
}