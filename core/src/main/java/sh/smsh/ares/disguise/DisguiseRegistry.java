package sh.smsh.ares.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseRegistry {

    public static final String UNKNOWN_HEAD_KEY = "unknown";

    @NotNull private static final Map<Integer, DisguiseBinding> byEntityId = new ConcurrentHashMap<>();
    @NotNull private static final Map<String, DisguiseBinding> byPlayerName = new ConcurrentHashMap<>();

    /**
     * Binds this player to this entity, replacing any previous entity for that player.
     */
    public static boolean tryRegister(
            final int entityId,
            @NotNull final String playerName,
            @Nullable final String rawCustomName,
            @NotNull final String headKey,
            final boolean selfInferred) {

        final String resolvedHeadKey = headKey.isEmpty() ? UNKNOWN_HEAD_KEY : headKey;
        final DisguiseBinding binding =
                new DisguiseBinding(entityId, playerName, rawCustomName, resolvedHeadKey, selfInferred);

        final DisguiseBinding previousByName = byPlayerName.put(playerName, binding);
        if (previousByName != null && previousByName.getEntityId() != entityId) {
            byEntityId.remove(previousByName.getEntityId(), previousByName);
        }

        final DisguiseBinding previousById = byEntityId.put(entityId, binding);
        if (previousById != null && !previousById.getPlayerName().equals(playerName)) {
            byPlayerName.remove(previousById.getPlayerName(), previousById);
        }

        return true;
    }

    public static void unregisterByEntityId(final int entityId) {
        final DisguiseBinding removed = byEntityId.remove(entityId);
        if (removed != null) {
            byPlayerName.remove(removed.getPlayerName(), removed);
        }
    }

    public static void unregisterByPlayer(@NotNull final String playerName) {
        final DisguiseBinding removed = byPlayerName.remove(playerName);
        if (removed != null) {
            byEntityId.remove(removed.getEntityId(), removed);
        }
    }

    @Nullable
    public static DisguiseBinding getByEntityId(final int entityId) {
        return byEntityId.get(entityId);
    }

    @Nullable
    public static DisguiseBinding getByPlayerName(@NotNull final String playerName) {
        return byPlayerName.get(playerName);
    }

    public static boolean isDisguiseEntity(final int entityId) {
        return byEntityId.containsKey(entityId);
    }

    @NotNull
    public static Collection<DisguiseBinding> getAll() {
        return Collections.unmodifiableCollection(byEntityId.values());
    }

    public static void clear() {
        byEntityId.clear();
        byPlayerName.clear();
    }
}
