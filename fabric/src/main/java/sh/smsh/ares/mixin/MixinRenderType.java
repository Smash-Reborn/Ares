package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.fabric.disguise.DisguiseGlowOcclusion;

import java.util.Optional;

@Mixin(RenderType.class)
public abstract class MixinRenderType {

    /**
     * While a disguise model is being submitted, its outline variant is the depth-tested one.
     * Every other caller still receives the vanilla see-through outline.
     */
    @Inject(method = "outline", at = @At("HEAD"), cancellable = true)
    private void ares$disguiseOutline(final CallbackInfoReturnable<Optional<RenderType>> cir) {
        if (!DisguiseGlowOcclusion.capturing()) {
            return;
        }
        final RenderType occluded = DisguiseGlowOcclusion.outlineFor((RenderType) (Object) this);
        if (occluded != null) {
            cir.setReturnValue(Optional.of(occluded));
        }
    }
}
