package sh.smsh.ares.disguise;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public final class MineplexNametagFix {

    private final int disguiseEntityId;
    @NotNull private final String playerName;
    private final int wolfEntityId;
    private final int armorStandEntityId;
    @NotNull private final String originalArmorStandCustomName;
}
