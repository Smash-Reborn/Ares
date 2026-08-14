package sh.smsh.ares.disguise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
     * Infer the local player's disguise head key from hotbar contents.
     * Used because the server does not send the local player their own disguise entity.
     * Independent of nametag-fix scaffolding (wolf / armor stand).
     *
     * @return lowercase head texture key, or {@code null} if not detectable
     */
    @Nullable
    String resolveSelfHeadKey(@NotNull List<SelfHotbarItem> hotbar);
}
