package sh.smsh.ares.disguise.nametag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public final class MineplexNametagFix {

    private final int disguiseEntityId;
    @NotNull private final String playerName;
    private final int wolfEntityId;
    private final int armorStandEntityId;
    @NotNull private final String originalArmorStandCustomName;
}
