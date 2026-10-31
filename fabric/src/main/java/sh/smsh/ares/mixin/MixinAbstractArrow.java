package sh.smsh.ares.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sh.smsh.ares.fabric.RenderDistanceApplier;
import sh.smsh.ares.feature.impl.ArrowBouncingDisableAnimationFeature;

@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow {

    /**
     * A missed hit calls {@code deflect(REVERSE)}, which flips the arrow and spins it.
     * Skipping that call leaves the rest of the hit path, including the failed-hit fire restore, intact.
     */
    @Redirect(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;deflect(Lnet/minecraft/world/entity/projectile/ProjectileDeflection;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityReference;ZD)Z"
            )
    )
    private boolean ares$skipMissBounce(
            final AbstractArrow arrow,
            final ProjectileDeflection deflection,
            final Entity hitEntity,
            final EntityReference<Entity> owner,
            final boolean fromAttack,
            final double speed) {

        if (ArrowBouncingDisableAnimationFeature.shouldDisableAnimation()) {
            return false;
        }

        return arrow.deflect(deflection, hitEntity, owner, fromAttack, speed);
    }

    @Redirect(
            method = "shouldRenderAtSqrDistance",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;getViewScale()D")
    )
    private double ares$extendArrowRenderDistance() {
        return RenderDistanceApplier.viewScaleFor((Entity) (Object) this);
    }
}
