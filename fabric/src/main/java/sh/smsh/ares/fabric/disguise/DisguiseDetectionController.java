package sh.smsh.ares.fabric.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.DisplaySlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseDetectionStrategy;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.disguise.DisguiseStrategies;
import sh.smsh.ares.disguise.self.SelfHotbarItem;
import sh.smsh.ares.feature.impl.DisguiseDetectionFeature;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseDetectionController {

    @NotNull private static Set<String> participatingPlayers = Set.of();
    @Nullable private static String lastSelfHeadKey;

    public static void onClientTick() {
        refreshParticipatingPlayers();
        refreshSelfDisguise();
    }

    public static void onEntityUpdated(final int entityId) {
        if (!DisguiseDetectionFeature.shouldApply()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || DisguiseStrategies.forCurrentServer() == null) {
            return;
        }

        considerLivingEntity(minecraft.level.getEntity(entityId), false);
    }

    public static void onEntitiesRemoved(@NotNull final int[] entityIds) {
        for (final int entityId : entityIds) {
            final DisguiseBinding binding = DisguiseRegistry.getByEntityId(entityId);
            if (binding != null && !binding.isSelfInferred()) {
                DisguiseRegistry.unregisterByEntityId(entityId);
            }
        }
    }

    public static void clear() {
        participatingPlayers = Set.of();
        lastSelfHeadKey = null;
        SelfDisguiseRenderController.clear();
        DisguiseRegistry.clear();
    }

    private static void considerLivingEntity(@Nullable final Entity entity, final boolean skipIfPlayerAlreadyTracked) {
        if (!(entity instanceof LivingEntity) || entity instanceof Player) {
            return;
        }

        // Armor stands and invisible wolves are Mineplex nametag scaffolding, not the disguise mob.
        if (entity instanceof ArmorStand || (entity instanceof Wolf && entity.isInvisible())) {
            return;
        }

        final DisguiseDetectionStrategy strategy = DisguiseStrategies.forCurrentServer();
        if (strategy == null) {
            return;
        }

        final String customName = entity.getCustomName() == null ? "" : entity.getCustomName().getString();
        if (customName.isEmpty()) {
            final DisguiseBinding existing = DisguiseRegistry.getByEntityId(entity.getId());
            if (existing != null && !existing.isSelfInferred()) {
                DisguiseRegistry.unregisterByEntityId(entity.getId());
            }
            return;
        }

        final String matchedPlayer = strategy.matchPlayerFromCustomName(customName, participatingPlayers);
        if (matchedPlayer == null) {
            return;
        }

        if (skipIfPlayerAlreadyTracked && DisguiseRegistry.getByPlayerName(matchedPlayer) != null) {
            return;
        }

        DisguiseRegistry.tryRegister(
                entity.getId(),
                matchedPlayer,
                customName,
                DisguiseHeads.resolveHeadKey(entity),
                false);
    }

    private static void refreshParticipatingPlayers() {
        if (!DisguiseDetectionFeature.shouldApply()) {
            if (!participatingPlayers.isEmpty() || !DisguiseRegistry.getAll().isEmpty()) {
                clear();
            }
            return;
        }

        final DisguiseDetectionStrategy strategy = DisguiseStrategies.forCurrentServer();
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (strategy == null || player == null || minecraft.level == null || minecraft.getConnection() == null) {
            participatingPlayers = Set.of();
            return;
        }

        final Set<String> nextParticipating = strategy.resolveParticipatingPlayers(
                collectTabNames(minecraft.getConnection()),
                collectSidebarLines(minecraft.level.getScoreboard()));

        final boolean changed = !nextParticipating.equals(participatingPlayers);
        pruneLeftPlayers(nextParticipating);
        participatingPlayers = nextParticipating;

        if (!participatingPlayers.isEmpty() && (changed || hasUntrackedParticipatingPlayers())) {
            for (final Entity entity : minecraft.level.entitiesForRendering()) {
                considerLivingEntity(entity, true);
            }
        }
    }

    private static boolean hasUntrackedParticipatingPlayers() {
        for (final String playerName : participatingPlayers) {
            if (DisguiseRegistry.getByPlayerName(playerName) == null) {
                return true;
            }
        }
        return false;
    }

    private static void refreshSelfDisguise() {
        if (!DisguiseDetectionFeature.shouldApply()) {
            return;
        }

        final DisguiseDetectionStrategy strategy = DisguiseStrategies.forCurrentServer();
        final LocalPlayer self = Minecraft.getInstance().player;
        if (strategy == null || self == null) {
            return;
        }

        final String selfName = self.getName().getString();
        if (selfName.isEmpty() || !participatingPlayers.contains(selfName)) {
            lastSelfHeadKey = null;
            clearSelfInferredBinding(selfName);
            return;
        }

        final String resolved = strategy.resolveSelfHeadKey(collectHotbarItems(self));
        final String headKey;
        if (resolved == null || resolved.isEmpty()) {
            if (hasRecognizedDisguise(lastSelfHeadKey) || DisguiseRegistry.UNKNOWN_HEAD_KEY.equals(lastSelfHeadKey)) {
                headKey = DisguiseRegistry.UNKNOWN_HEAD_KEY;
                SelfDisguiseRenderController.clear();
            } else {
                lastSelfHeadKey = null;
                clearSelfInferredBinding(selfName);
                SelfDisguiseRenderController.clear();
                return;
            }
        } else {
            headKey = resolved;
        }

        lastSelfHeadKey = headKey;
        final DisguiseBinding existing = DisguiseRegistry.getByPlayerName(selfName);
        if (existing != null
                && existing.isSelfInferred()
                && existing.getEntityId() == self.getId()
                && headKey.equals(existing.getHeadKey())) {
            return;
        }

        DisguiseRegistry.tryRegister(self.getId(), selfName, null, headKey, true);
    }

    private static boolean hasRecognizedDisguise(@Nullable final String headKey) {
        return headKey != null
                && !headKey.isEmpty()
                && !DisguiseRegistry.UNKNOWN_HEAD_KEY.equals(headKey);
    }

    private static void clearSelfInferredBinding(@NotNull final String playerName) {
        if (playerName.isEmpty()) {
            return;
        }

        final DisguiseBinding existing = DisguiseRegistry.getByPlayerName(playerName);
        if (existing != null && existing.isSelfInferred()) {
            DisguiseRegistry.unregisterByPlayer(playerName);
        }
    }

    private static void pruneLeftPlayers(@NotNull final Set<String> nextParticipating) {
        final List<String> toRemove = new ArrayList<>();
        for (final DisguiseBinding binding : DisguiseRegistry.getAll()) {
            if (!nextParticipating.contains(binding.getPlayerName())) {
                toRemove.add(binding.getPlayerName());
            }
        }

        for (final String playerName : toRemove) {
            DisguiseRegistry.unregisterByPlayer(playerName);
        }
    }

    @NotNull
    private static Collection<String> collectTabNames(@NotNull final ClientPacketListener connection) {
        final List<String> names = new ArrayList<>();
        for (final PlayerInfo info : connection.getListedOnlinePlayers()) {
            if (info == null || info.getProfile() == null || info.getProfile().name() == null) {
                continue;
            }

            final String name = info.getProfile().name();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    @NotNull
    private static Collection<String> collectSidebarLines(@Nullable final Scoreboard scoreboard) {
        if (scoreboard == null) {
            return List.of();
        }

        final Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) {
            return List.of();
        }

        final List<String> lines = new ArrayList<>();
        for (final PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
            if (entry == null || entry.isHidden()) {
                continue;
            }

            if (entry.display() != null) {
                lines.add(entry.display().getString());
            }
            if (entry.owner() != null) {
                lines.add(entry.owner());
            }
        }
        return lines;
    }

    @NotNull
    private static List<SelfHotbarItem> collectHotbarItems(@NotNull final LocalPlayer self) {
        final List<SelfHotbarItem> items = new ArrayList<>(9);
        for (int slot = 0; slot < 9; slot++) {
            final ItemStack stack = self.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                items.add(new SelfHotbarItem("", ""));
                continue;
            }

            final String itemKey = String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            final String displayName = TextFormattingUtil.stripFormatting(stack.getHoverName().getString());
            items.add(new SelfHotbarItem(itemKey, displayName));
        }
        return items;
    }
}
