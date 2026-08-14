package sh.smsh.ares.disguise.nametag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MineplexNametagFixRegistry {

    @NotNull private static final Map<Integer, MineplexNametagFix> byDisguiseEntityId = new ConcurrentHashMap<>();
    @NotNull private static final Map<Integer, MineplexNametagFix> byWolfEntityId = new ConcurrentHashMap<>();
    @NotNull private static final Map<Integer, MineplexNametagFix> byArmorStandEntityId = new ConcurrentHashMap<>();
    @NotNull private static final Map<String, MineplexNametagFix> byPlayerName = new ConcurrentHashMap<>();

    public static void register(@NotNull final MineplexNametagFix fix) {
        remove(fix.getDisguiseEntityId());
        byDisguiseEntityId.put(fix.getDisguiseEntityId(), fix);
        byWolfEntityId.put(fix.getWolfEntityId(), fix);
        byArmorStandEntityId.put(fix.getArmorStandEntityId(), fix);
        byPlayerName.put(fix.getPlayerName(), fix);
    }

    public static void remove(final int disguiseEntityId) {
        final MineplexNametagFix removed = byDisguiseEntityId.remove(disguiseEntityId);
        if (removed != null) {
            byWolfEntityId.remove(removed.getWolfEntityId(), removed);
            byArmorStandEntityId.remove(removed.getArmorStandEntityId(), removed);
            byPlayerName.remove(removed.getPlayerName(), removed);
        }
    }

    public static void removeByPlayer(@NotNull final String playerName) {
        final MineplexNametagFix removed = byPlayerName.remove(playerName);
        if (removed != null) {
            byDisguiseEntityId.remove(removed.getDisguiseEntityId(), removed);
            byWolfEntityId.remove(removed.getWolfEntityId(), removed);
            byArmorStandEntityId.remove(removed.getArmorStandEntityId(), removed);
        }
    }

    @Nullable public static MineplexNametagFix getByDisguiseEntityId(final int disguiseEntityId) {
        return byDisguiseEntityId.get(disguiseEntityId);
    }

    @Nullable public static MineplexNametagFix getByWolfEntityId(final int wolfEntityId) {
        return byWolfEntityId.get(wolfEntityId);
    }

    @Nullable public static MineplexNametagFix getByArmorStandEntityId(final int armorStandEntityId) {
        return byArmorStandEntityId.get(armorStandEntityId);
    }

    @Nullable public static MineplexNametagFix getByPlayerName(@NotNull final String playerName) {
        return byPlayerName.get(playerName);
    }

    public static boolean isNametagFixEntity(final int entityId) {
        return byDisguiseEntityId.containsKey(entityId)
                || byWolfEntityId.containsKey(entityId)
                || byArmorStandEntityId.containsKey(entityId);
    }

    @NotNull public static Collection<MineplexNametagFix> getAll() {
        return Collections.unmodifiableCollection(byDisguiseEntityId.values());
    }

    public static void clear() {
        byDisguiseEntityId.clear();
        byWolfEntityId.clear();
        byArmorStandEntityId.clear();
        byPlayerName.clear();
    }
}
