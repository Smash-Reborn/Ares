package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.feature.impl.NativeNametagFeature;
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

    @Redirect(
            method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/IChatComponent;getFormattedText()Ljava/lang/String;"))
    private String ares$colorDisguiseNametag(
            final IChatComponent displayName,
            final EntityLivingBase entity,
            final double x,
            final double y,
            final double z) {
        final String formattedText = displayName.getFormattedText();
        if (!MineplexNametagFixController.shouldForceRenderDisguiseName(entity)) {
            return formattedText;
        }

        return NativeNametagFeature.getNametagColor().applyTo(formattedText);
    }
}
