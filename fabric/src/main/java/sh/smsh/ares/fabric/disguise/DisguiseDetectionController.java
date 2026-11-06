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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import java.util.LinkedHashSet;
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

    public static void onEntitySpawned(final int entityId) {
        DisguiseSquids.noteSpawn(entityId);
        onEntityUpdated(entityId);
    }

    public static void onEntityUpdated(final int entityId) {
        if (!DisguiseDetectionFeature.shouldApply()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || DisguiseStrategies.forCurrentServer() == null) {
            return;
        }

        considerEntity(minecraft.level.getEntity(entityId));
        DisguiseSquids.observe(minecraft.level.getEntity(entityId));
        DisguiseSquids.tryClaim();
    }

    public static void onEntitiesRemoved(@NotNull final int[] entityIds) {
        DisguiseSquids.forget(entityIds);
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
        DisguiseSquids.clear();
        DisguiseRegistry.clear();
    }

    private static void considerEntity(@Nullable final Entity entity) {
        if (entity == null || entity instanceof Player) {
            return;
        }

        final DisguiseDetectionStrategy strategy = DisguiseStrategies.forCurrentServer();
        if (strategy == null) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        final String matchedPlayer = matchedPlayer(strategy, entity);
        if (matchedPlayer == null) {
            final DisguiseBinding existing = DisguiseRegistry.getByEntityId(entity.getId());
            if (existing != null && !existing.isSelfInferred()) {
                DisguiseRegistry.unregisterByEntityId(entity.getId());
            }
            return;
        }

        registerDisguise(entity, matchedPlayer, entity.getCustomName().getString());
    }

    private static void registerDisguise(
            @NotNull final Entity mob,
            @NotNull final String playerName,
            @Nullable final String rawCustomName) {

        final DisguiseBinding existing = DisguiseRegistry.getByPlayerName(playerName);
        if (existing != null && existing.getEntityId() != mob.getId() && disguiseStillValid(existing)) {
            return;
        }

        DisguiseRegistry.tryRegister(
                mob.getId(),
                playerName,
                rawCustomName,
                DisguiseHeads.resolveHeadKey(mob),
                false);
    }

    @Nullable
    private static String matchedPlayer(@NotNull final DisguiseDetectionStrategy strategy, @NotNull final Entity entity) {
        if (entity.getCustomName() == null) {
            return null;
        }
        return strategy.matchPlayerFromCustomName(entity.getCustomName().getString(), participatingPlayers);
    }

    private static boolean disguiseStillValid(@NotNull final DisguiseBinding binding) {
        if (binding.isSelfInferred()) {
            return false;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }

        final Entity entity = minecraft.level.getEntity(binding.getEntityId());
        if (!(entity instanceof LivingEntity) || entity instanceof Player) {
            return false;
        }

        return nameMatches(entity, binding.getPlayerName());
    }

    private static boolean nameMatches(@Nullable final Entity entity, @NotNull final String playerName) {
        final DisguiseDetectionStrategy strategy = DisguiseStrategies.forCurrentServer();
        if (strategy == null || entity == null || entity.getCustomName() == null) {
            return false;
        }

        return playerName.equals(strategy.matchPlayerFromCustomName(entity.getCustomName().getString(), participatingPlayers));
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

        final Set<String> nextParticipating = new LinkedHashSet<>(collectTabNames(minecraft.getConnection()));

        pruneLeftPlayers(nextParticipating);
        participatingPlayers = nextParticipating;

        if (!participatingPlayers.isEmpty()) {
            for (final Entity entity : minecraft.level.entitiesForRendering()) {
                considerEntity(entity);
            }
            pruneStaleDisguises(minecraft);
        }
    }

    private static void pruneStaleDisguises(@NotNull final Minecraft minecraft) {
        if (minecraft.level == null) {
            return;
        }

        final LocalPlayer self = minecraft.player;
        final List<Integer> stale = new ArrayList<>();
        for (final DisguiseBinding binding : DisguiseRegistry.getAll()) {
            if (binding.isSelfInferred() && self != null && binding.getEntityId() == self.getId()) {
                continue;
            }
            if (!disguiseStillValid(binding)) {
                stale.add(binding.getEntityId());
            }
        }

        for (final int entityId : stale) {
            DisguiseRegistry.unregisterByEntityId(entityId);
        }
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

        final DisguiseBinding liveDisguise = DisguiseRegistry.getByPlayerName(selfName);
        if (liveDisguise != null && !liveDisguise.isSelfInferred() && disguiseStillValid(liveDisguise)) {
            lastSelfHeadKey = liveDisguise.getHeadKey();
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
