package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sh.smsh.ares.fabric.disguise.DisguiseGlowOcclusion;

import java.util.Optional;

/**
 * Item outlines are resolved after entity submit has finished. Disguise item
 * submits use the depth-tested outline; every other glow keeps the vanilla one.
 */
@Mixin(ItemFeatureRenderer.class)
public abstract class MixinItemFeatureRenderer {

    @Redirect(
            method = "prepareOutlineSubmit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/rendertype/RenderType;outline()Ljava/util/Optional;"))
    private Optional<RenderType> ares$occludedItemOutline(
            final RenderType type, final ItemFeatureRenderer.Submit submit) {
        if (DisguiseGlowOcclusion.isItem(submit)) {
            final RenderType occluded = DisguiseGlowOcclusion.outlineFor(type);
            if (occluded != null) {
                return Optional.of(occluded);
            }
        }
        return type.outline();
    }
}
