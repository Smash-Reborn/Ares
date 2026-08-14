package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.forge.disguise.MineplexNametagFixController;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity {

    @Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLivingBase;)Z", at = @At("HEAD"), cancellable = true)
    private void ares$forceDisguiseNametag(
            final EntityLivingBase entity,
            final CallbackInfoReturnable<Boolean> cir) {
        if (MineplexNametagFixController.shouldForceRenderDisguiseName(entity)) {
            cir.setReturnValue(true);
        }
    }
}
