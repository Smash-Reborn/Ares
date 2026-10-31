package sh.smsh.ares.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerType;
import sh.smsh.ares.disguise.strategy.MineplexDisguiseDetectionStrategy;
import sh.smsh.ares.gate.ServerGate;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseStrategies {

    @NotNull private static final List<DisguiseDetectionStrategy> STRATEGIES = List.of(
            new MineplexDisguiseDetectionStrategy()
    );

    @Nullable
    public static DisguiseDetectionStrategy forCurrentServer() {
        return forServerType(ServerGate.getCurrentServerType());
    }

    @Nullable
    public static DisguiseDetectionStrategy forServerType(@NotNull final ServerType serverType) {
        for (final DisguiseDetectionStrategy strategy : STRATEGIES) {
            if (strategy.supports(serverType)) {
                return strategy;
            }
        }

        return null;
    }
}
