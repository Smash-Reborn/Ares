package sh.smsh.ares.forge.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityWolf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.feature.impl.NativeNametagFeature;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wither Skeleton kit "Wither Image" minions are extra wither skeletons named after the owner.
 * Mineplex also mounts the same invis wolf + armor-stand nametag stack on them, so both the
 * skeleton and matching armor-stand custom names get the configured color.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WitherImageNametagController {

    private static final String WITHER_SKELETON_HEAD_KEY = "witherskeleton";

    /** Wither-image entity id -> owner player name (stripped). */
    @NotNull private static final Map<Integer, String> witherImages = new ConcurrentHashMap<>();

    public static void consider(@Nullable final Entity entity) {
        if (!(entity instanceof EntitySkeleton)) {
            return;
        }

        if (((EntitySkeleton) entity).getSkeletonType() != 1) {
            return;
        }

        final int entityId = entity.getEntityId();
        if (DisguiseRegistry.isDisguiseEntity(entityId)) {
            return;
        }

        final String customName = entity.getCustomNameTag();
        if (customName == null || customName.isEmpty()) {
            witherImages.remove(entityId);
            return;
        }

        final String stripped = TextFormattingUtil.stripFormatting(customName);
        if (stripped.isEmpty()) {
            return;
        }

        final DisguiseBinding owner = DisguiseRegistry.getByPlayerName(stripped);
        if (owner == null || !WITHER_SKELETON_HEAD_KEY.equals(owner.getHeadKey())) {
            return;
        }

        witherImages.put(entityId, owner.getPlayerName());
        applyNametagStack(entity, owner.getPlayerName());
    }

    public static void onClientTick() {
        if (witherImages.isEmpty()) {
            return;
        }

        reapplyAll();
    }

    public static void onEntityAttach(final int passengerId, final int vehicleId) {
        if (witherImages.isEmpty() || vehicleId < 0) {
            return;
        }

        refreshFromEntityId(vehicleId);
        refreshFromEntityId(passengerId);
    }

    public static void onEntityMetadata(final int entityId) {
        if (witherImages.isEmpty()) {
            return;
        }

        refreshFromEntityId(entityId);
    }

    public static void onEntitiesDestroyed(@NotNull final int[] entityIds) {
        for (final int entityId : entityIds) {
            witherImages.remove(entityId);
        }
    }

    public static void clear() {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null) {
            for (final Integer entityId : witherImages.keySet()) {
                final Entity entity = mc.theWorld.getEntityByID(entityId);
                if (entity != null) {
                    stripNametag(entity);
                    final EntityArmorStand armorStand = findNametagArmorStand(entity);
                    if (armorStand != null) {
                        stripNametag(armorStand);
                    }
                }
            }
        }

        witherImages.clear();
    }

    private static void refreshFromEntityId(final int entityId) {
        if (witherImages.containsKey(entityId)) {
            applyForId(entityId);
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }

        final Entity entity = mc.theWorld.getEntityByID(entityId);
        if (entity == null) {
            return;
        }

        Entity root = entity;
        while (root.ridingEntity != null) {
            root = root.ridingEntity;
        }

        if (witherImages.containsKey(root.getEntityId())) {
            applyNametagStack(root, witherImages.get(root.getEntityId()));
        }
    }

    private static void reapplyAll() {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            witherImages.clear();
            return;
        }

        final Iterator<Map.Entry<Integer, String>> iterator = witherImages.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Integer, String> entry = iterator.next();
            final Entity entity = mc.theWorld.getEntityByID(entry.getKey());
            if (entity == null) {
                iterator.remove();
                continue;
            }

            applyNametagStack(entity, entry.getValue());
        }
    }

    private static void applyForId(final int witherImageEntityId) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return;
        }

        final Entity entity = mc.theWorld.getEntityByID(witherImageEntityId);
        final String ownerName = witherImages.get(witherImageEntityId);
        if (entity == null || ownerName == null) {
            return;
        }

        applyNametagStack(entity, ownerName);
    }

    private static void applyNametagStack(@NotNull final Entity witherImage, @NotNull final String ownerName) {
        applyNametagColor(witherImage);

        final EntityArmorStand armorStand = findNametagArmorStand(witherImage);
        if (armorStand == null) {
            return;
        }

        final String standName = armorStand.getCustomNameTag();
        if (standName == null || standName.isEmpty()) {
            return;
        }

        if (!ownerName.equals(TextFormattingUtil.stripFormatting(standName))) {
            return;
        }

        applyNametagColor(armorStand);
    }

    @Nullable
    private static EntityArmorStand findNametagArmorStand(@NotNull final Entity witherImage) {
        final Entity wolf = witherImage.riddenByEntity;
        if (!(wolf instanceof EntityWolf) || !wolf.isInvisible()) {
            return null;
        }

        final Entity rider = wolf.riddenByEntity;
        if (!(rider instanceof EntityArmorStand) || !rider.isInvisible()) {
            return null;
        }

        return (EntityArmorStand) rider;
    }

    private static void applyNametagColor(@NotNull final Entity entity) {
        final String current = entity.getCustomNameTag();
        if (current == null || current.isEmpty()) {
            return;
        }

        final String colored = NativeNametagFeature.getNametagColor().applyTo(current);
        if (!colored.equals(current)) {
            entity.setCustomNameTag(colored);
        }
    }

    private static void stripNametag(@NotNull final Entity entity) {
        final String current = entity.getCustomNameTag();
        if (current == null || current.isEmpty()) {
            return;
        }

        final String stripped = TextFormattingUtil.stripFormatting(current);
        if (!stripped.equals(current)) {
            entity.setCustomNameTag(stripped);
        }
    }
}
