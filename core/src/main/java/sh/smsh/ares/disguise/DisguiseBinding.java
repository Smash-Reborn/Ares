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
}
