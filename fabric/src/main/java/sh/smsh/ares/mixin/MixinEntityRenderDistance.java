package sh.smsh.ares.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sh.smsh.ares.fabric.RenderDistanceApplier;

@Mixin(Entity.class)
public abstract class MixinEntityRenderDistance {

    @Redirect(
            method = "shouldRenderAtSqrDistance",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;viewScale:D")
    )
    private double ares$extendRenderDistance() {
        return RenderDistanceApplier.viewScaleFor((Entity) (Object) this);
    }
}
