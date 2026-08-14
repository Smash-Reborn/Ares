package sh.smsh.ares.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.forge.disguise.MineplexNametagFixController;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    @Inject(method = "getAlwaysRenderNameTagForRender()Z", at = @At("HEAD"), cancellable = true)
    private void ares$forceDisguiseNametag(final CallbackInfoReturnable<Boolean> cir) {
        if (MineplexNametagFixController.shouldForceRenderDisguiseName((Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
