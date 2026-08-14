package sh.smsh.ares.forge.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseDetectionStrategy;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.disguise.DisguiseStrategies;
import sh.smsh.ares.disguise.MineplexNametagFixRegistry;
import sh.smsh.ares.feature.impl.DisguiseDetectionFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseDetectionController {

    @NotNull private static Set<String> participatingPlayers = Collections.emptySet();

    public static void onClientTick() {
        refreshParticipatingPlayers();
        MineplexNametagFixController.onClientTick();
    }

    public static void onLivingEntitySpawnOrMetadata(final int entityId) {
        if (!DisguiseDetectionFeature.shouldApply()) {
            return;
        }

        final DisguiseDetectionStrategy strategy = DisguiseStrategies.forCurrentServer();
        if (strategy == null) {
            return;
        }

        refreshParticipatingPlayers();

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }

        final Entity entity = mc.theWorld.getEntityByID(entityId);
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }

        // armor stands are mineplex nametag scaffolding, never the disguise itself.
        // invisible wolves are also nametag scaffolding on mineplex; visible wolves can be real
        // kit disguises. HACKY: invisibility is used as a heuristic and may need improving later.
        if (entity instanceof EntityArmorStand
                || (entity instanceof EntityWolf && entity.isInvisible())) {
            return;
        }

        final String customName = entity.getCustomNameTag();

        if (customName == null || customName.isEmpty()) {
            if (DisguiseRegistry.isDisguiseEntity(entityId)) {
                MineplexNametagFixController.onDisguiseUnregistered(entityId);
                DisguiseRegistry.unregisterByEntityId(entityId);
            }
            return;
        }

        final String matchedPlayer = strategy.matchPlayerFromCustomName(customName, participatingPlayers);

        if (matchedPlayer != null) {
            DisguiseRegistry.tryRegister(entityId, matchedPlayer, customName);
        }
    }

    public static void onEntitiesDestroyed(final int[] entityIds) {
        if (!DisguiseDetectionFeature.shouldApply()) {
            MineplexNametagFixController.onEntitiesDestroyed(entityIds);
            return;
        }

        MineplexNametagFixController.onEntitiesDestroyed(entityIds);
    }

    public static void clear() {
        participatingPlayers = Collections.emptySet();
        MineplexNametagFixController.clear();
        DisguiseRegistry.clear();
    }

    private static boolean hasTrackedState() {
        return !participatingPlayers.isEmpty()
                || !DisguiseRegistry.getAll().isEmpty()
                || !MineplexNametagFixRegistry.getAll().isEmpty();
    }

    private static void refreshParticipatingPlayers() {
        if (!DisguiseDetectionFeature.shouldApply()) {
            if (hasTrackedState()) {
                clear();
            } else {
                participatingPlayers = Collections.emptySet();
            }
            return;
        }

        final DisguiseDetectionStrategy strategy = DisguiseStrategies.forCurrentServer();

        if (strategy == null) {
            participatingPlayers = Collections.emptySet();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.sendQueue == null) {
            participatingPlayers = Collections.emptySet();
            return;
        }

        final Set<String> nextParticipating = strategy.resolveParticipatingPlayers(
                collectTabNames(mc),
                collectSidebarLines(mc.theWorld.getScoreboard()));

        pruneLeftPlayers(nextParticipating);
        participatingPlayers = nextParticipating;
    }

    private static void pruneLeftPlayers(@NotNull final Set<String> nextParticipating) {
        final List<String> toRemove = new ArrayList<>();

        for (final DisguiseBinding binding : DisguiseRegistry.getAll()) {
            if (!nextParticipating.contains(binding.getPlayerName())) {
                toRemove.add(binding.getPlayerName());
            }
        }

        for (final String playerName : toRemove) {
            MineplexNametagFixController.onPlayerUnregistered(playerName);
            DisguiseRegistry.unregisterByPlayer(playerName);
        }
    }

    @NotNull
    private static Collection<String> collectTabNames(@NotNull final Minecraft mc) {
        final Collection<NetworkPlayerInfo> infos = mc.thePlayer.sendQueue.getPlayerInfoMap();

        if (infos == null || infos.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> names = new ArrayList<>(infos.size());

        for (final NetworkPlayerInfo info : infos) {
            if (info == null || info.getGameProfile() == null) {
                continue;
            }

            final String name = info.getGameProfile().getName();

            if (name != null && !name.isEmpty()) {
                names.add(name);
            }
        }

        return names;
    }

    @NotNull
    private static Collection<String> collectSidebarLines(@Nullable final Scoreboard scoreboard) {
        if (scoreboard == null) {
            return Collections.emptyList();
        }

        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

        if (objective == null) {
            return Collections.emptyList();
        }

        final Collection<Score> scores = scoreboard.getSortedScores(objective);

        if (scores == null || scores.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> lines = new ArrayList<>(scores.size());

        for (final Score score : scores) {
            if (score == null) {
                continue;
            }

            final String entry = score.getPlayerName();
            if (entry == null || entry.isEmpty()) {
                continue;
            }

            final ScorePlayerTeam team = scoreboard.getPlayersTeam(entry);
            lines.add(ScorePlayerTeam.formatPlayerName(team, entry));
        }

        return lines;
    }
}
