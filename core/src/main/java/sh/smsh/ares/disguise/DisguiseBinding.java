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
    /** Texture key under assets/aresmod/textures/gui/mob_heads, without {@code .png}. */
    @NotNull private final String headKey;
    /** True when inferred from the local player's hotbar. The server does not spawn that mob. */
    private final boolean selfInferred;
}
