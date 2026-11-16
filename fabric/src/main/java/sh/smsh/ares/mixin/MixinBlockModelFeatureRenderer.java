package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sh.smsh.ares.fabric.disguise.DisguiseGlowOcclusion;

/**
 * Disguise block sprites (mooshroom mushrooms) are drawn from the outline type
 * stored on the submit. Swap that type for the depth-tested outline at draw time.
 */
@Mixin(BlockModelFeatureRenderer.class)
public abstract class MixinBlockModelFeatureRenderer {

    @Redirect(
            method = "buildGroup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/BlockModelFeatureRenderer$Submit;renderType()Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType ares$occludedBlockOutline(final BlockModelFeatureRenderer.Submit submit) {
        final RenderType type = submit.renderType();
        if (!DisguiseGlowOcclusion.isBlockModel(submit)) {
            return type;
        }
        if (type.isOutline()) {
            final RenderType occluded = DisguiseGlowOcclusion.blockOutlineFor(type);
            return occluded != null ? occluded : type;
        }
        return DisguiseGlowOcclusion.blockSolidFor(type);
    }
}
