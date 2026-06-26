package sh.smsh.ares.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.forge.RenderDistanceApplier;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient {

    @Inject(method = "addEntityToWorld", at = @At("RETURN"))
    private void ares$applyRenderDistance(int entityIdIn, Entity entityIn, CallbackInfo ci) {
        if (entityIn != null) {
            RenderDistanceApplier.apply(entityIn);
        }
    }
}
