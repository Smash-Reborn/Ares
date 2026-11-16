package sh.smsh.ares.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.fabric.disguise.DisguiseGlowOcclusion;

/**
 * Block models (mooshroom mushrooms, carried blocks) resolve their outline here.
 * A type that is already an outline skips {@link RenderType#outline()}, so the
 * depth-tested variant has to be applied to the returned type as well.
 */
@Mixin(SubmitNodeCollection.class)
public abstract class MixinSubmitNodeCollection {

    @Inject(method = "getOutlineRenderType", at = @At("RETURN"), cancellable = true)
    private static void ares$occludedOutline(
            final RenderType source, final CallbackInfoReturnable<RenderType> cir) {
        if (!DisguiseGlowOcclusion.capturing()) {
            return;
        }
        RenderType occluded = DisguiseGlowOcclusion.outlineFor(source);
        if (occluded == null) {
            final RenderType current = cir.getReturnValue();
            if (current != null) {
                occluded = DisguiseGlowOcclusion.outlineFor(current);
            }
        }
        if (occluded != null) {
            cir.setReturnValue(occluded);
        }
    }

    /**
     * The outline submit is the second constructor in {@code submitBlockModel}.
     * Shrink that pose in block space. The solid mushroom keeps the original pose.
     */
    @ModifyArg(
            method = "submitBlockModel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/BlockModelFeatureRenderer$Submit;<init>(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/rendertype/RenderType;Ljava/util/List;[IIIILcom/mojang/blaze3d/vertex/PoseStack$Pose;)V",
                    ordinal = 1),
            index = 0)
    private PoseStack.Pose ares$insetBlockOutlinePose(final PoseStack.Pose pose) {
        if (!DisguiseGlowOcclusion.capturing() || pose == null) {
            return pose;
        }
        return DisguiseGlowOcclusion.insetBlockOutline(pose);
    }

    @ModifyArg(
            method = "submitBlockModel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/BlockModelFeatureRenderer$Submit;<init>(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/rendertype/RenderType;Ljava/util/List;[IIIILcom/mojang/blaze3d/vertex/PoseStack$Pose;)V",
                    ordinal = 1),
            index = 1)
    private RenderType ares$blockOutlineType(final RenderType type) {
        if (!DisguiseGlowOcclusion.capturing()) {
            return type;
        }
        final RenderType occluded = DisguiseGlowOcclusion.blockOutlineFor(type);
        return occluded != null ? occluded : type;
    }
}
