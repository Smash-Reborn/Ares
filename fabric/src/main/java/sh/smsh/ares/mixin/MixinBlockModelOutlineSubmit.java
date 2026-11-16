package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.fabric.disguise.DisguiseGlowOcclusion;

/**
 * Mooshroom mushrooms are block sprites submitted with the cow. Remember those
 * submits so the outline draw still depth-tests them after entity submit ends.
 */
@Mixin(BlockModelFeatureRenderer.Submit.class)
public abstract class MixinBlockModelOutlineSubmit {

    @Inject(
            method = "<init>(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/rendertype/RenderType;Ljava/util/List;[IIIILcom/mojang/blaze3d/vertex/PoseStack$Pose;)V",
            at = @At("RETURN"))
    private void ares$markDisguiseBlockModel(final CallbackInfo ci) {
        if (DisguiseGlowOcclusion.capturing()) {
            DisguiseGlowOcclusion.markBlockModel(this);
        }
    }
}
