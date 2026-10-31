package sh.smsh.ares.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import sh.smsh.ares.fabric.RenderDistanceApplier;

/**
 * Eggs, pearls, potions, and fireballs ignore {@code Entity.viewScale} and bake 64 into their own distance check.
 */
@Mixin({ThrowableProjectile.class, AbstractHurtingProjectile.class})
public abstract class MixinProjectileRenderDistance {

    @ModifyConstant(method = "shouldRenderAtSqrDistance", constant = @Constant(doubleValue = 64.0))
    private double ares$extendProjectileRenderDistance(final double distanceConstant) {
        return RenderDistanceApplier.scaleDistanceConstant((Entity) (Object) this, distanceConstant);
    }
}
