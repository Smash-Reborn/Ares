package sh.smsh.ares.fabric.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SidebarLines {

    @NotNull
    public static List<String> current() {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return List.of();
        }

        final Scoreboard scoreboard = minecraft.level.getScoreboard();
        final Objective objective = displayedSidebar(scoreboard, player);
        if (objective == null) {
            return List.of();
        }

        final List<String> lines = new ArrayList<>();
        for (final PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
            if (entry == null || entry.isHidden() || entry.owner() == null) {
                continue;
            }

            final PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
            final Component name = PlayerTeam.formatNameForTeam(team, entry.ownerName());
            if (name != null) {
                lines.add(name.getString());
            }
        }
        return lines;
    }

    @Nullable
    private static Objective displayedSidebar(@NotNull final Scoreboard scoreboard, @NotNull final LocalPlayer player) {
        final PlayerTeam team = scoreboard.getPlayersTeam(player.getScoreboardName());
        if (team != null) {
            final Optional<TeamColor> color = team.getColor();
            if (color.isPresent()) {
                final Objective teamObjective = scoreboard.getDisplayObjective(color.get().displaySlot());
                if (teamObjective != null) {
                    return teamObjective;
                }
            }
        }
        return scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
    }
}
