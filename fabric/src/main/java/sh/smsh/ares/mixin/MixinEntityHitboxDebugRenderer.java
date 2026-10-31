package sh.smsh.ares.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.fabric.disguise.DisguiseHitboxMargin;
import sh.smsh.ares.feature.impl.HighlightMeleeBoundingBoxFeature;
import sh.smsh.ares.settings.ModSettings;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class MixinEntityHitboxDebugRenderer {

    @Unique private Entity ares$entity;

    @Inject(method = "showHitboxes", at = @At("HEAD"))
    private void ares$captureEntity(final Entity entity, final float partialTick, final boolean green, final CallbackInfo ci) {
        this.ares$entity = entity;
    }

    @Redirect(
            method = "showHitboxes",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            )
    )
    private AABB ares$scaleDisguiseBox(final Entity entity) {
        return DisguiseHitboxMargin.boxFor(entity);
    }

    @ModifyConstant(method = "showHitboxes", constant = @Constant(intValue = -1))
    private int ares$highlightMeleeRange(final int color) {
        if (!HighlightMeleeBoundingBoxFeature.shouldApply() || this.ares$entity == null) {
            return color;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null || player == this.ares$entity || !isLookingAt(minecraft, this.ares$entity)) {
            return color;
        }

        final AABB box = DisguiseHitboxMargin.boxFor(this.ares$entity);
        final double range = player.entityInteractionRange();
        if (player.isWithinAttackRange(player.getMainHandItem(), box, range)) {
            return ModSettings.getHighlightMeleeBoundingBoxColor().getArgb();
        }

        return color;
    }

    @Unique
    private static boolean isLookingAt(final Minecraft minecraft, final Entity entity) {
        if (minecraft.crosshairPickEntity == entity) {
            return true;
        }

        final HitResult hit = minecraft.hitResult;
        return hit instanceof EntityHitResult entityHit && entityHit.getEntity() == entity;
    }
}
