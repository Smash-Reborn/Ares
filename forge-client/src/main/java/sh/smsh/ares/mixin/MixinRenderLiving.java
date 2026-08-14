package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.forge.disguise.MineplexNametagFixController;

@Mixin(RenderLiving.class)
public abstract class MixinRenderLiving {

    @Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLiving;)Z", at = @At("HEAD"), cancellable = true)
    private void ares$forceDisguiseNametag(
            final EntityLiving entity,
            final CallbackInfoReturnable<Boolean> cir) {

        if (MineplexNametagFixController.shouldForceRenderDisguiseName(entity)) {
            cir.setReturnValue(true);
        }
    }
}
