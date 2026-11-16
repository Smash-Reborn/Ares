package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sh.smsh.ares.fabric.disguise.DisguiseGlowOcclusion;

/**
 * Mooshroom mushrooms and carried blocks are outline submits in this renderer.
 * While a disguise is glowing, draw those outlines with the cow's depth-tested pipeline.
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
        if (type.isOutline() && DisguiseGlowOcclusion.hasMarks()) {
            return DisguiseGlowOcclusion.blockSpriteOutline();
        }
        return type;
    }
}
