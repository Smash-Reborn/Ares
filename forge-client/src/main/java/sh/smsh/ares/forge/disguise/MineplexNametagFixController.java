package sh.smsh.ares.forge.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityWolf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerType;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.disguise.nametag.MineplexNametagFix;
import sh.smsh.ares.disguise.nametag.MineplexNametagFixRegistry;
import sh.smsh.ares.feature.impl.DisguiseDetectionFeature;
import sh.smsh.ares.gate.ServerGate;
import sh.smsh.ares.settings.ModSettings;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MineplexNametagFixController {

    private static final int ARMOR_STAND_OBJECT_TYPE = 78;

    /** Invisible wolves waiting to attach to a tracked disguise. */
    @NotNull private static final Map<Integer, Boolean> pendingInvisibleWolves = new ConcurrentHashMap<>();

    /** Wolf entity id -> disguise entity id after wolf mounts the disguise. */
    @NotNull private static final Map<Integer, Integer> wolfToDisguise = new ConcurrentHashMap<>();

    /** Invisible armor stands with a custom name matching a tracked player (entityId -> playerName). */
    @NotNull private static final Map<Integer, String> pendingArmorStands = new ConcurrentHashMap<>();

    private static boolean lastUseNativeNametag = true;

    public static void onClientTick() {
        if (!isMineplexActive()) {
            if (!pendingInvisibleWolves.isEmpty()
                    || !wolfToDisguise.isEmpty()
                    || !pendingArmorStands.isEmpty()
                    || !MineplexNametagFixRegistry.getAll().isEmpty()) {

                clear();
            }

            return;
        }

        syncNativeNametagSetting();
        enforceBlankArmorStandNames();
    }

    /**
     * Catch-up after participating players become known. Spawn/attach packets may already have
     * been applied, so rebuild wolf and armor-stand links from the current world state.
     */
    public static void rescanLoadedEntities() {
        if (!isMineplexActive()) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }

        for (final Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityWolf && entity.isInvisible()) {
                pendingInvisibleWolves.put(entity.getEntityId(), true);
            } else if (entity instanceof EntityArmorStand) {
                considerArmorStandCandidate(entity);
            }
        }

        for (final Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityWolf && entity.ridingEntity != null) {
                onEntityAttach(entity.getEntityId(), entity.ridingEntity.getEntityId());
            }
        }

        for (final Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand && entity.ridingEntity != null) {
                onEntityAttach(entity.getEntityId(), entity.ridingEntity.getEntityId());
            }
        }
    }

    public static void onMobSpawn(final int entityId) {
        if (!isMineplexActive()) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null) {
            return;
        }

        final Entity entity = mc.theWorld.getEntityByID(entityId);

        if (entity instanceof EntityWolf && entity.isInvisible()) {
            pendingInvisibleWolves.put(entityId, true);
        }
    }

    public static void onObjectSpawn(final int entityId, final int type) {
        if (!isMineplexActive() || type != ARMOR_STAND_OBJECT_TYPE) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null) {
            return;
        }

        considerArmorStandCandidate(mc.theWorld.getEntityByID(entityId));
    }

    public static void onEntityMetadata(final int entityId) {
        if (!isMineplexActive()) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null) {
            return;
        }

        final Entity entity = mc.theWorld.getEntityByID(entityId);

        if (entity instanceof EntityWolf && entity.isInvisible()) {
            pendingInvisibleWolves.put(entityId, true);
        } else if (entity instanceof EntityArmorStand) {
            considerArmorStandCandidate(entity);

            final MineplexNametagFix fix = MineplexNametagFixRegistry.getByArmorStandEntityId(entityId);

            if (fix != null && ModSettings.shouldUseNativeNametag()) {
                blankArmorStandName((EntityArmorStand) entity);
            }
        }
    }

    public static void onEntityAttach(final int passengerId, final int vehicleId) {
        if (!isMineplexActive()) {
            return;
        }

        if (vehicleId < 0) {
            pendingInvisibleWolves.remove(passengerId);
            wolfToDisguise.remove(passengerId);
            pendingArmorStands.remove(passengerId);
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }

        final Entity passenger = mc.theWorld.getEntityByID(passengerId);
        final Entity vehicle = mc.theWorld.getEntityByID(vehicleId);

        if (passenger == null || vehicle == null) {
            return;
        }

        if (passenger instanceof EntityWolf && DisguiseRegistry.isDisguiseEntity(vehicleId)) {
            if (passenger.isInvisible() || pendingInvisibleWolves.containsKey(passengerId)) {
                pendingInvisibleWolves.remove(passengerId);
                wolfToDisguise.put(passengerId, vehicleId);
            }
            return;
        }

        if (passenger instanceof EntityArmorStand && vehicle instanceof EntityWolf) {
            final Integer disguiseEntityId = wolfToDisguise.get(vehicleId);
            if (disguiseEntityId == null) {
                return;
            }

            final DisguiseBinding disguise = DisguiseRegistry.getByEntityId(disguiseEntityId);
            if (disguise == null) {
                return;
            }

            String playerName = pendingArmorStands.remove(passengerId);
            if (playerName == null) {
                if (!passenger.isInvisible()) {
                    return;
                }
                playerName = matchTrackedPlayerName(passenger.getCustomNameTag());
            }

            if (playerName == null || !playerName.equals(disguise.getPlayerName())) {
                return;
            }

            final String originalName = passenger.getCustomNameTag() == null ? "" : passenger.getCustomNameTag();
            final String storedName = originalName.isEmpty()
                    ? (disguise.getRawCustomName() == null ? disguise.getPlayerName() : disguise.getRawCustomName())
                    : originalName;

            final MineplexNametagFix fix = new MineplexNametagFix(
                    disguiseEntityId,
                    disguise.getPlayerName(),
                    vehicleId,
                    passengerId,
                    storedName);
            MineplexNametagFixRegistry.register(fix);

            if (ModSettings.shouldUseNativeNametag()) {
                blankArmorStandName((EntityArmorStand) passenger);
            }
        }
    }

    public static void onEntitiesDestroyed(final int[] entityIds) {
        for (final int entityId : entityIds) {
            pendingInvisibleWolves.remove(entityId);
            pendingArmorStands.remove(entityId);

            final Integer disguiseFromWolf = wolfToDisguise.remove(entityId);

            if (disguiseFromWolf != null) {
                removeFixForDisguise(disguiseFromWolf, true);
            }

            final MineplexNametagFix byWolf = MineplexNametagFixRegistry.getByWolfEntityId(entityId);

            if (byWolf != null) {
                removeFixForDisguise(byWolf.getDisguiseEntityId(), true);
            }

            final MineplexNametagFix byArmorStand = MineplexNametagFixRegistry.getByArmorStandEntityId(entityId);

            if (byArmorStand != null) {
                removeFixForDisguise(byArmorStand.getDisguiseEntityId(), false);
            }

            if (DisguiseRegistry.isDisguiseEntity(entityId)) {
                removeFixForDisguise(entityId, true);
                DisguiseRegistry.unregisterByEntityId(entityId);
            }
        }
    }

    public static void onNativeNametagSettingChanged() {
        lastUseNativeNametag = ModSettings.shouldUseNativeNametag();
        if (lastUseNativeNametag) {
            applyNativeNametag();
        } else {
            revertNativeNametag();
        }
    }

    public static int getTrackedWolfEntityIdForDisguise(final int disguiseEntityId) {
        final MineplexNametagFix fix = MineplexNametagFixRegistry.getByDisguiseEntityId(disguiseEntityId);
        if (fix != null) {
            return fix.getWolfEntityId();
        }

        for (final Map.Entry<Integer, Integer> entry : wolfToDisguise.entrySet()) {
            if (entry.getValue() == disguiseEntityId) {
                return entry.getKey();
            }
        }

        return -1;
    }

    public static boolean shouldForceRenderDisguiseName(@Nullable final Entity entity) {
        if (entity == null || !ModSettings.shouldUseNativeNametag() || !isMineplexActive()) {
            return false;
        }

        if (!DisguiseRegistry.isDisguiseEntity(entity.getEntityId())) {
            return false;
        }

        return MineplexNametagFixRegistry.getByDisguiseEntityId(entity.getEntityId()) != null;
    }

    public static void onDisguiseUnregistered(final int disguiseEntityId) {
        removeFixForDisguise(disguiseEntityId, true);
    }

    public static void onPlayerUnregistered(@NotNull final String playerName) {
        final MineplexNametagFix fix = MineplexNametagFixRegistry.getByPlayerName(playerName);

        if (fix != null) {
            removeFixForDisguise(fix.getDisguiseEntityId(), true);
        }
    }

    public static void clear() {
        clearPending();
        revertNativeNametag();
        MineplexNametagFixRegistry.clear();
        lastUseNativeNametag = ModSettings.shouldUseNativeNametag();
    }

    private static void clearPending() {
        pendingInvisibleWolves.clear();
        wolfToDisguise.clear();
        pendingArmorStands.clear();
    }

    private static void syncNativeNametagSetting() {
        final boolean current = ModSettings.shouldUseNativeNametag();
        if (current == lastUseNativeNametag) {
            return;
        }

        lastUseNativeNametag = current;

        if (current) {
            applyNativeNametag();
        } else {
            revertNativeNametag();
        }
    }

    private static void applyNativeNametag() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null) {
            return;
        }

        for (final MineplexNametagFix fix : MineplexNametagFixRegistry.getAll()) {
            final Entity entity = mc.theWorld.getEntityByID(fix.getArmorStandEntityId());

            if (entity instanceof EntityArmorStand) {
                blankArmorStandName((EntityArmorStand) entity);
            }
        }
    }

    private static void revertNativeNametag() {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }

        for (final MineplexNametagFix fix : MineplexNametagFixRegistry.getAll()) {
            final Entity entity = mc.theWorld.getEntityByID(fix.getArmorStandEntityId());

            if (entity instanceof EntityArmorStand) {
                entity.setCustomNameTag(fix.getOriginalArmorStandCustomName());
                entity.setAlwaysRenderNameTag(true);
            }
        }
    }

    private static void enforceBlankArmorStandNames() {
        if (!ModSettings.shouldUseNativeNametag()) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null) {
            return;
        }

        for (final MineplexNametagFix fix : MineplexNametagFixRegistry.getAll()) {
            final Entity entity = mc.theWorld.getEntityByID(fix.getArmorStandEntityId());

            if (entity instanceof EntityArmorStand) {
                final EntityArmorStand armorStand = (EntityArmorStand) entity;

                if (armorStand.getCustomNameTag() != null && !armorStand.getCustomNameTag().isEmpty()) {
                    blankArmorStandName(armorStand);
                }
            }
        }
    }

    private static void blankArmorStandName(@NotNull final EntityArmorStand armorStand) {
        armorStand.setCustomNameTag("");
        armorStand.setAlwaysRenderNameTag(false);
    }

    private static void considerArmorStandCandidate(@Nullable final Entity entity) {
        if (!(entity instanceof EntityArmorStand) || !entity.isInvisible()) {
            return;
        }

        final String matchedPlayer = matchTrackedPlayerName(entity.getCustomNameTag());

        if (matchedPlayer != null) {
            pendingArmorStands.put(entity.getEntityId(), matchedPlayer);
        }
    }

    @Nullable private static String matchTrackedPlayerName(@Nullable final String customName) {
        if (customName == null || customName.isEmpty()) {
            return null;
        }

        final String stripped = TextFormattingUtil.stripFormatting(customName);

        if (stripped.isEmpty()) {
            return null;
        }

        for (final DisguiseBinding binding : DisguiseRegistry.getAll()) {
            if (stripped.equals(binding.getPlayerName())) {
                return binding.getPlayerName();
            }
        }

        return null;
    }

    private static void removeFixForDisguise(final int disguiseEntityId, final boolean restoreArmorStand) {
        final MineplexNametagFix fix = MineplexNametagFixRegistry.getByDisguiseEntityId(disguiseEntityId);

        if (fix == null) {
            return;
        }

        removeWolfLinksForDisguise(disguiseEntityId);

        if (restoreArmorStand && ModSettings.shouldUseNativeNametag()) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.theWorld != null) {
                final Entity entity = mc.theWorld.getEntityByID(fix.getArmorStandEntityId());

                if (entity instanceof EntityArmorStand) {
                    entity.setCustomNameTag(fix.getOriginalArmorStandCustomName());
                    entity.setAlwaysRenderNameTag(true);
                }
            }
        }

        MineplexNametagFixRegistry.remove(disguiseEntityId);
    }

    private static void removeWolfLinksForDisguise(final int disguiseEntityId) {
        final Iterator<Map.Entry<Integer, Integer>> iterator = wolfToDisguise.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<Integer, Integer> entry = iterator.next();

            if (entry.getValue() == disguiseEntityId) {
                iterator.remove();
            }
        }
    }

    private static boolean isMineplexActive() {
        return DisguiseDetectionFeature.shouldApply()
                && ServerGate.getCurrentServerType() == ServerType.MINEPLEX;
    }
}
