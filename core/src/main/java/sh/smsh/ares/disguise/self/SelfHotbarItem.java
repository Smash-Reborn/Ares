package sh.smsh.ares.disguise.self;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Minecraft-free snapshot of one hotbar stack. {@code itemKey} is a registry id such as
 * {@code minecraft:iron_sword}, or empty when the slot is empty.
 */
@Getter
@RequiredArgsConstructor
public final class SelfHotbarItem {

    @NotNull private final String itemKey;
    @NotNull private final String displayName;

    public boolean isEmpty() {
        return itemKey.isEmpty();
    }
}
