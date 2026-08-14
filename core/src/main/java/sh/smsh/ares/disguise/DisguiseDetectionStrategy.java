package sh.smsh.ares.disguise;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerType;

import java.util.Collection;
import java.util.Set;

public interface DisguiseDetectionStrategy {

    boolean supports(@NotNull final ServerType serverType);

    @NotNull Set<String> resolveParticipatingPlayers(
            @NotNull final Collection<String> tabNames,
            @NotNull final Collection<String> sidebarLines);

    @Nullable String matchPlayerFromCustomName(
            @Nullable final String customName,
            @NotNull final Set<String> participatingPlayers);
}
