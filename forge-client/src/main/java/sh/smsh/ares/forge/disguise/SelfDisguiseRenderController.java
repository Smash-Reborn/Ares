package sh.smsh.ares.forge.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.AresLog;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.feature.impl.SelfDisguiseEntityFeature;

import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelfDisguiseRenderController {

    private static final int DUMMY_ENTITY_ID = -870041;

    @Nullable private static EntityLivingBase dummyEntity;
    @Nullable private static String dummyHeadKey;
    @Nullable private static World dummyWorld;
    private static boolean renderingSelfDisguise;

    public static boolean tryRenderInstead(
            @Nullable final Entity entity,
            final double x,
            final double y,
            final double z,
            final float entityYaw,
            final float partialTicks,
            final boolean hideDebugBox) {
        if (renderingSelfDisguise) {
            return false;
        }

        if (!SelfDisguiseEntityFeature.shouldApply()) {
            clear();
            return false;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayerSP self = mc.thePlayer;
        if (self == null || entity != self || self.isSpectator() || self.isInvisible()) {
            return false;
        }

        final String selfName = self.getName();
        if (selfName == null || selfName.isEmpty()) {
            return false;
        }

        final DisguiseBinding binding = DisguiseRegistry.getByPlayerName(selfName);
        final String headKey = normalizeHeadKey(binding == null ? null : binding.getHeadKey());
        if (headKey == null) {
            clear();
            return false;
        }

        final EntityLivingBase dummy = getOrCreateDummy(mc, headKey);
        if (dummy == null) {
            clear();
            return false;
        }

        final RenderManager renderManager = mc.getRenderManager();
        final Render<Entity> render = renderManager.getEntityRenderObject(dummy);

        if (render == null) {
            clear();
            return false;
        }

        copyPose(self, dummy);

        renderingSelfDisguise = true;
        try {
            renderManager.doRenderEntity(dummy, x, y, z, entityYaw, partialTicks, hideDebugBox);
            return true;
        } catch (Exception ex) {
            AresLog.LOGGER.warn("Failed to render self disguise entity for head key '{}'.", headKey, ex);
            clear();
            return false;
        } finally {
            renderingSelfDisguise = false;
        }
    }

    public static void clear() {
        dummyEntity = null;
        dummyHeadKey = null;
        dummyWorld = null;
    }

    @Nullable
    private static EntityLivingBase getOrCreateDummy(
            @NotNull final Minecraft mc,
            @NotNull final String headKey) {
        if (mc.theWorld == null) {
            return null;
        }

        if (dummyEntity != null
                && dummyWorld == mc.theWorld
                && headKey.equals(dummyHeadKey)
                && dummyEntity.worldObj == mc.theWorld) {
            return dummyEntity;
        }

        final EntityLivingBase created = createDisguiseEntity(mc.theWorld, headKey);
        dummyEntity = created;
        dummyHeadKey = created == null ? null : headKey;
        dummyWorld = created == null ? null : mc.theWorld;
        return created;
    }

    @Nullable
    private static EntityLivingBase createDisguiseEntity(
            @NotNull final World world,
            @NotNull final String headKey) {
        final Entity created = createFromEntityList(world, resolveEntityListName(headKey));
        if (!(created instanceof EntityLivingBase)) {
            return null;
        }

        final EntityLivingBase living = (EntityLivingBase) created;
        applyVariant(living, headKey);
        living.setEntityId(DUMMY_ENTITY_ID);
        living.setCustomNameTag("");
        living.setAlwaysRenderNameTag(false);
        living.noClip = true;
        return living;
    }

    @Nullable
    private static Entity createFromEntityList(@NotNull final World world, @NotNull final String name) {
        final Entity direct = EntityList.createEntityByName(name, world);
        if (direct != null) {
            return direct;
        }

        for (final String rawName : EntityList.stringToClassMapping.keySet()) {
            if (rawName instanceof String && rawName.equalsIgnoreCase(name)) {
                return EntityList.createEntityByName(rawName, world);
            }
        }

        return null;
    }

    @NotNull
    private static String resolveEntityListName(@NotNull final String headKey) {
        if ("witherskeleton".equals(headKey)) {
            return "Skeleton";
        }
        if ("skeletonhorse".equals(headKey) || "zombiehorse".equals(headKey) || "donkey".equals(headKey)) {
            return "EntityHorse";
        }
        return headKey;
    }

    private static void applyVariant(@NotNull final EntityLivingBase entity, @NotNull final String headKey) {
        if (entity instanceof EntitySkeleton && "witherskeleton".equals(headKey)) {
            ((EntitySkeleton) entity).setSkeletonType(1);
        } else if (entity instanceof EntityHorse) {
            final EntityHorse horse = (EntityHorse) entity;
            if ("skeletonhorse".equals(headKey)) {
                horse.setHorseType(4);
            } else if ("zombiehorse".equals(headKey)) {
                horse.setHorseType(3);
            } else if ("donkey".equals(headKey)) {
                horse.setHorseType(1);
            }
            horse.setGrowingAge(0);
        } else if (entity instanceof EntityAgeable) {
            ((EntityAgeable) entity).setGrowingAge(0);
        }
    }

    @Nullable
    private static String normalizeHeadKey(@Nullable final String headKey) {
        if (headKey == null || headKey.isEmpty()) {
            return null;
        }

        final String key = headKey.toLowerCase(Locale.ROOT).replace(",", "").trim();
        if (key.isEmpty() || DisguiseRegistry.UNKNOWN_HEAD_KEY.equals(key)) {
            return null;
        }

        return key;
    }

    private static void copyPose(@NotNull final EntityPlayer player, @NotNull final EntityLivingBase dummy) {
        dummy.worldObj = player.worldObj;
        dummy.dimension = player.dimension;
        dummy.setPosition(player.posX, player.posY, player.posZ);

        dummy.prevPosX = player.prevPosX;
        dummy.prevPosY = player.prevPosY;
        dummy.prevPosZ = player.prevPosZ;
        dummy.lastTickPosX = player.lastTickPosX;
        dummy.lastTickPosY = player.lastTickPosY;
        dummy.lastTickPosZ = player.lastTickPosZ;
        dummy.posX = player.posX;
        dummy.posY = player.posY;
        dummy.posZ = player.posZ;

        dummy.motionX = player.motionX;
        dummy.motionY = player.motionY;
        dummy.motionZ = player.motionZ;

        dummy.rotationYaw = player.rotationYaw;
        dummy.rotationPitch = player.rotationPitch;
        dummy.prevRotationYaw = player.prevRotationYaw;
        dummy.prevRotationPitch = player.prevRotationPitch;
        dummy.rotationYawHead = player.rotationYawHead;
        dummy.prevRotationYawHead = player.prevRotationYawHead;
        dummy.renderYawOffset = player.renderYawOffset;
        dummy.prevRenderYawOffset = player.prevRenderYawOffset;

        dummy.limbSwing = player.limbSwing;
        dummy.limbSwingAmount = player.limbSwingAmount;
        dummy.prevLimbSwingAmount = player.prevLimbSwingAmount;
        dummy.swingProgress = player.swingProgress;
        dummy.prevSwingProgress = player.prevSwingProgress;
        dummy.isSwingInProgress = player.isSwingInProgress;

        dummy.hurtTime = player.hurtTime;
        dummy.maxHurtTime = player.maxHurtTime;
        dummy.hurtResistantTime = player.hurtResistantTime;
        dummy.deathTime = player.deathTime;
        dummy.ticksExisted = player.ticksExisted;
        dummy.onGround = player.onGround;
        dummy.isAirBorne = player.isAirBorne;
        dummy.distanceWalkedModified = player.distanceWalkedModified;
        dummy.prevDistanceWalkedModified = player.prevDistanceWalkedModified;

        dummy.setSneaking(player.isSneaking());
        dummy.setSprinting(player.isSprinting());
        dummy.setInvisible(false);

        if (player.isBurning()) {
            dummy.setFire(1);
        } else {
            dummy.extinguish();
        }
    }
}
