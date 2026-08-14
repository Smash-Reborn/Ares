package sh.smsh.ares.disguise;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Minecraft-free snapshot of one hotbar stack for self-disguise kit detection.
 */
@Getter
@RequiredArgsConstructor
public final class SelfHotbarItem {

    /** Vanilla item ID, or {@code -1} if empty. */
    private final int itemId;
    /** Damage / metadata value. */
    private final int metadata;
    /** Display name with formatting codes stripped; empty if unnamed / empty slot. */
    @NotNull private final String displayName;
}
