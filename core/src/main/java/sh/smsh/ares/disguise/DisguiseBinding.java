package sh.smsh.ares.disguise;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public final class DisguiseBinding {

    private final int entityId;
    @NotNull private final String playerName;
    @Nullable private final String rawCustomName;
    /** Lowercase texture key under assets/.../mob_heads (e.g. wither skeleton) */
    @NotNull private final String headKey;
    /** True when registered from the local player's hotbar kit inference (no server disguise entity) */
    private final boolean selfInferred;
}
