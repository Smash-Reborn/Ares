package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.fabric.disguise.SelfDisguiseRenderController;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {

    @Inject(method = "extractEntity", at = @At("HEAD"), cancellable = true)
    private void ares$renderSelfDisguise(
            final Entity entity,
            final float partialTick,
            final CallbackInfoReturnable<EntityRenderState> cir) {

        if (SelfDisguiseRenderController.isRendering()) {
            return;
        }

        final Entity replacement = SelfDisguiseRenderController.replacementFor(entity);
        if (replacement == null) {
            return;
        }

        SelfDisguiseRenderController.beginRendering();
        try {
            cir.setReturnValue(((EntityRenderDispatcher) (Object) this).extractEntity(replacement, partialTick));
        } finally {
            SelfDisguiseRenderController.endRendering();
        }
    }
}
