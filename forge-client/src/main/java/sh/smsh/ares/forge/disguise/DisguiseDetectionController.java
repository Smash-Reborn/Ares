package sh.smsh.ares.forge.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import sh.smsh.ares.disguise.nametag.MineplexNametagFixRegistry;
import sh.smsh.ares.disguise.self.SelfHotbarItem;
import sh.smsh.ares.feature.impl.DisguiseDetectionFeature;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseDetectionController {

    @NotNull private static Set<String> participatingPlayers = Collections.emptySet();
    @Nullable private static String lastSelfHeadKey;

    public static void onClientTick() {
        refreshParticipatingPlayers();
        refreshSelfDisguise();
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

        considerLivingEntity(mc.theWorld.getEntityByID(entityId), false);
    }

    private static void considerLivingEntity(@Nullable final Entity entity, final boolean skipIfPlayerAlreadyTracked) {
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }

        // Players are never disguise mobs. The local player's entity id is reused for the
        // self-inferred tab head; treating them as a nameless disguise would unregister it.
        if (entity instanceof EntityPlayer
                || entity instanceof EntityArmorStand
                || (entity instanceof EntityWolf && entity.isInvisible())) {
            return;
        }

        final String customName = entity.getCustomNameTag();
        final int entityId = entity.getEntityId();

        if (customName == null || customName.isEmpty()) {
            final DisguiseBinding existing = DisguiseRegistry.getByEntityId(entityId);
            if (existing != null && !existing.isSelfInferred()) {
                MineplexNametagFixController.onDisguiseUnregistered(entityId);
                DisguiseRegistry.unregisterByEntityId(entityId);
            }

            return;
        }

        final DisguiseDetectionStrategy strategy = DisguiseStrategies.forCurrentServer();
        if (strategy == null) {
            return;
        }

        final String matchedPlayer = strategy.matchPlayerFromCustomName(customName, participatingPlayers);

        if (matchedPlayer == null) {
            return;
        }

        if (skipIfPlayerAlreadyTracked && DisguiseRegistry.getByPlayerName(matchedPlayer) != null) {
            return;
        }

        final String headKey = DisguiseHeadTypeResolver.resolveHeadKey(entity);
        DisguiseRegistry.tryRegister(entityId, matchedPlayer, customName, headKey, false);
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
        lastSelfHeadKey = null;
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

        final boolean participatingChanged = !nextParticipating.equals(participatingPlayers);
        pruneLeftPlayers(nextParticipating);
        participatingPlayers = nextParticipating;

        if (!participatingPlayers.isEmpty()
                && (participatingChanged || hasUntrackedParticipatingPlayers())) {
            rescanLoadedEntitiesForMissedDisguises();
        }
    }

    /**
     * Spawns during world transition can arrive before tab/scoreboard participation is known.
     * Re-check loaded entities once names are available so those disguises are not missed.
     */
    private static void rescanLoadedEntitiesForMissedDisguises() {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }

        for (final Entity entity : mc.theWorld.loadedEntityList) {
            considerLivingEntity(entity, true);
        }

        MineplexNametagFixController.rescanLoadedEntities();
    }

    private static boolean hasUntrackedParticipatingPlayers() {
        for (final String playerName : participatingPlayers) {
            if (DisguiseRegistry.getByPlayerName(playerName) == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Local player never receives their own disguise mob spawn. Infer kit from hotbar when they
     * are a participating player (tab ∩ sidebar). Does not require nametag-fix wolf/armor-stand.
     */
    private static void refreshSelfDisguise() {
        if (!DisguiseDetectionFeature.shouldApply()) {
            return;
        }

        final DisguiseDetectionStrategy strategy = DisguiseStrategies.forCurrentServer();
        if (strategy == null) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayerSP self = mc.thePlayer;
        if (self == null) {
            return;
        }

        final String selfName = self.getName();
        if (selfName == null || selfName.isEmpty() || !participatingPlayers.contains(selfName)) {
            lastSelfHeadKey = null;
            clearSelfInferredBinding(selfName);
            return;
        }

        String headKey = strategy.resolveSelfHeadKey(collectHotbarItems(self));
        if (headKey == null || headKey.isEmpty()) {
            headKey = lastSelfHeadKey;
        }
        if (headKey == null || headKey.isEmpty()) {
            return;
        }

        lastSelfHeadKey = headKey;

        final DisguiseBinding existing = DisguiseRegistry.getByPlayerName(selfName);
        if (existing != null
                && existing.isSelfInferred()
                && existing.getEntityId() == self.getEntityId()
                && headKey.equals(existing.getHeadKey())) {
            return;
        }

        DisguiseRegistry.tryRegister(self.getEntityId(), selfName, null, headKey, true);
    }

    private static void clearSelfInferredBinding(@Nullable final String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return;
        }

        final DisguiseBinding existing = DisguiseRegistry.getByPlayerName(playerName);
        if (existing != null && existing.isSelfInferred()) {
            DisguiseRegistry.unregisterByPlayer(playerName);
        }
    }

    @NotNull
    private static List<SelfHotbarItem> collectHotbarItems(@NotNull final EntityPlayerSP self) {
        final List<SelfHotbarItem> items = new ArrayList<SelfHotbarItem>(9);
        for (int slot = 0; slot < 9; slot++) {
            final ItemStack stack = self.inventory.mainInventory[slot];
            if (stack == null || stack.getItem() == null) {
                items.add(new SelfHotbarItem(-1, 0, ""));
                continue;
            }

            final int itemId = Item.getIdFromItem(stack.getItem());
            final int metadata = stack.getMetadata();
            final String rawName = stack.hasDisplayName() ? stack.getDisplayName() : "";
            final String displayName = TextFormattingUtil.stripFormatting(rawName);
            items.add(new SelfHotbarItem(itemId, metadata, displayName));
        }
        return items;
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

    @NotNull private static Collection<String> collectTabNames(@NotNull final Minecraft mc) {
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

    @NotNull private static Collection<String> collectSidebarLines(@Nullable final Scoreboard scoreboard) {
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
