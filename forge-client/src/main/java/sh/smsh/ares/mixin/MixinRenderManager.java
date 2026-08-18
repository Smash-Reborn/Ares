package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.forge.disguise.DisguiseDebugBoxRenderer;
import sh.smsh.ares.forge.disguise.SelfDisguiseRenderController;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager {

    @Shadow
    public abstract boolean isDebugBoundingBox();

    @Inject(method = "doRenderEntity", at = @At("HEAD"), cancellable = true)
    private void ares$replaceSelfDisguise(
            final Entity entity,
            final double x,
            final double y,
            final double z,
            final float entityYaw,
            final float partialTicks,
            final boolean hideDebugBox,
            final CallbackInfoReturnable<Boolean> cir) {
        if (SelfDisguiseRenderController.tryRenderInstead(entity, x, y, z, entityYaw, partialTicks, hideDebugBox)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "doRenderEntity", at = @At("RETURN"))
    private void ares$renderDisguiseDebugBoxes(
            final Entity entity,
            final double x,
            final double y,
            final double z,
            final float entityYaw,
            final float partialTicks,
            final boolean hideDebugBox,
            final CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }

        DisguiseDebugBoxRenderer.onEntityRendered(entity, x, y, z, hideDebugBox, this.isDebugBoundingBox());
    }
}
