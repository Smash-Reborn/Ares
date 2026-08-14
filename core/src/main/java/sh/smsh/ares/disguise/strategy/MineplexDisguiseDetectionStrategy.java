package sh.smsh.ares.disguise.strategy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerType;
import sh.smsh.ares.disguise.DisguiseDetectionStrategy;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class MineplexDisguiseDetectionStrategy implements DisguiseDetectionStrategy {

    @Override
    public boolean supports(@NotNull final ServerType serverType) {
        return serverType == ServerType.MINEPLEX;
    }

    @Override
    @NotNull public Set<String> resolveParticipatingPlayers(
            @NotNull final Collection<String> tabNames,
            @NotNull final Collection<String> sidebarLines) {

        if (tabNames.isEmpty() || sidebarLines.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<String> strippedSidebar = new LinkedHashSet<>();

        for (final String line : sidebarLines) {

            final String stripped = TextFormattingUtil.stripFormatting(line);

            if (!stripped.isEmpty()) {
                strippedSidebar.add(stripped);
            }
        }

        if (strippedSidebar.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<String> participating = new LinkedHashSet<>();

        for (final String tabName : tabNames) {

            if (tabName == null || tabName.isEmpty()) {
                continue;
            }

            final String strippedName = TextFormattingUtil.stripFormatting(tabName);

            if (strippedName.isEmpty()) {
                continue;
            }

            for (final String sidebarLine : strippedSidebar) {

                if (sidebarLine.contains(strippedName)) {
                    participating.add(strippedName);
                    break;
                }
            }
        }

        return participating;
    }

    @Override
    @Nullable public String matchPlayerFromCustomName(
            @Nullable final String customName,
            @NotNull final Set<String> participatingPlayers) {

        if (customName == null || customName.isEmpty() || participatingPlayers.isEmpty()) {
            return null;
        }

        final String stripped = TextFormattingUtil.stripFormatting(customName);

        if (stripped.isEmpty()) {
            return null;
        }

        for (final String playerName : participatingPlayers) {

            if (stripped.equals(playerName)) {
                return playerName;
            }
        }

        return null;
    }
}
