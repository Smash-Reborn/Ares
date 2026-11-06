package sh.smsh.ares.disguise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerType;
import sh.smsh.ares.disguise.self.SelfHotbarItem;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * One server's rules for deciding who is in the match and which mob is their disguise.
 * Add an implementation and register it in {@link DisguiseStrategies} to support another network.
 */
public interface DisguiseDetectionStrategy {

    boolean supports(@NotNull ServerType serverType);

    @NotNull
    Set<String> resolveParticipatingPlayers(
            @NotNull Collection<String> tabNames,
            @NotNull Collection<String> sidebarLines);

    @Nullable
    String matchPlayerFromCustomName(
            @Nullable String customName,
            @NotNull Set<String> participatingPlayers);

    /**
     * The local player never receives their own disguise mob.
     *
     * @return head texture key, or null when the hotbar does not match a known kit
     */
    @Nullable
    String resolveSelfHeadKey(@NotNull List<SelfHotbarItem> hotbar);
}
