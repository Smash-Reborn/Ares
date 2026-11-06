package sh.smsh.ares.fabric.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.feature.impl.SelfDisguiseEntityFeature;
import sh.smsh.ares.settings.ModSettings;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelfDisguiseRenderController {

    private static boolean rendering;
    private static boolean preservePlayerState;
    @Nullable private static LivingEntity dummy;
    @Nullable private static String dummyHeadKey;
    private static int lastWalkTick = Integer.MIN_VALUE;

    public static boolean isRendering() {
        return rendering;
    }

    public static void setPreservePlayerState(final boolean preservePlayerState) {
        SelfDisguiseRenderController.preservePlayerState = preservePlayerState;
    }

    @Nullable
    public static Entity replacementFor(@NotNull final Entity entity) {
        if (rendering || preservePlayerState || !SelfDisguiseEntityFeature.shouldApply()) {
            return null;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer self = minecraft.player;
        if (self == null || entity != self || self.isSpectator() || self.isInvisible() || minecraft.level == null) {
            return null;
        }

        final String selfName = self.getName().getString();
        final DisguiseBinding binding = DisguiseRegistry.getByPlayerName(selfName);
        if (binding == null) {
            return null;
        }

        final EntityType<?> type = DisguiseHeads.entityTypeForHead(binding.getHeadKey());
        if (type == null) {
            return null;
        }

        if (dummy == null || dummy.level() != minecraft.level || !binding.getHeadKey().equals(dummyHeadKey)) {
            final Entity created = type.create(minecraft.level, EntitySpawnReason.COMMAND);
            if (!(created instanceof LivingEntity living)) {
                dummy = null;
                dummyHeadKey = null;
                return null;
            }

            dummy = living;
            dummyHeadKey = binding.getHeadKey();
        }

        copyPose(dummy, self);
        return dummy;
    }

    private static void copyPose(@NotNull final LivingEntity dummy, @NotNull final LocalPlayer self) {
        dummy.setId(self.getId());
        dummy.setPos(self.getX(), self.getY(), self.getZ());
        dummy.xo = self.xo;
        dummy.yo = self.yo;
        dummy.zo = self.zo;
        dummy.xOld = self.xOld;
        dummy.yOld = self.yOld;
        dummy.zOld = self.zOld;
        dummy.setYRot(self.getYRot());
        dummy.yRotO = self.yRotO;
        dummy.setXRot(self.getXRot());
        dummy.xRotO = self.xRotO;
        dummy.yBodyRot = self.yBodyRot;
        dummy.yBodyRotO = self.yBodyRotO;
        dummy.yHeadRot = self.yHeadRot;
        dummy.yHeadRotO = self.yHeadRotO;
        dummy.setPose(self.getPose());
        dummy.setOnGround(self.onGround());
        dummy.setDeltaMovement(self.getDeltaMovement());
        dummy.tickCount = self.tickCount;
        syncWalk(dummy, self);
    }

    /**
     * Limb, spike, and other appendage motion comes from how far the entity moved since the last tick.
     * A real mob applies that once per tick and the renderer interpolates it. Applying it on every
     * position copy makes those parts flail.
     */
    private static void syncWalk(@NotNull final LivingEntity dummy, @NotNull final LocalPlayer self) {
        if (ModSettings.isSelfDisguiseOnCrack()) {
            dummy.calculateEntityAnimation(false);
            return;
        }

        if (self.tickCount == lastWalkTick) {
            return;
        }

        lastWalkTick = self.tickCount;
        dummy.calculateEntityAnimation(false);
    }

    public static void beginRendering() {
        rendering = true;
    }

    public static void endRendering() {
        rendering = false;
    }

    public static void clear() {
        dummy = null;
        dummyHeadKey = null;
        lastWalkTick = Integer.MIN_VALUE;
        rendering = false;
        preservePlayerState = false;
    }
}
