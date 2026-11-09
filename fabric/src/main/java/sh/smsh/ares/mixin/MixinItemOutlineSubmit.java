package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.fabric.disguise.DisguiseGlowOcclusion;

/**
 * Held items (bows, blocks) build their outline later than the entity submit.
 * Remember the submits created while a disguise is being submitted so that
 * later outline lookup still depth-tests only those items.
 */
@Mixin(ItemFeatureRenderer.Submit.class)
public abstract class MixinItemOutlineSubmit {

    @Inject(
            method = "<init>(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V",
            at = @At("RETURN"))
    private void ares$markDisguiseItem(final CallbackInfo ci) {
        if (DisguiseGlowOcclusion.capturing()) {
            DisguiseGlowOcclusion.markItem(this);
        }
    }
}
