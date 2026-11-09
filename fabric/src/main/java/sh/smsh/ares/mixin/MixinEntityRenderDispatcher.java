package sh.smsh.ares.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.fabric.disguise.DisguiseGlowController;
import sh.smsh.ares.fabric.disguise.DisguiseGlowOcclusion;
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

    @Inject(method = "extractEntity", at = @At("RETURN"))
    private void ares$markDisguiseGlow(
            final Entity entity,
            final float partialTick,
            final CallbackInfoReturnable<EntityRenderState> cir) {
        final EntityRenderState state = cir.getReturnValue();
        if (state != null && DisguiseGlowController.occludesGlow(entity)) {
            DisguiseGlowOcclusion.mark(state);
        }
    }

    @Redirect(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
            )
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void ares$submitDisguiseGlow(
            final EntityRenderer renderer,
            final EntityRenderState state,
            final PoseStack poseStack,
            final SubmitNodeCollector collector,
            final CameraRenderState camera) {
        final boolean occlude = DisguiseGlowOcclusion.isMarked(state);
        DisguiseGlowOcclusion.push(occlude);
        try {
            renderer.submit(state, poseStack, collector, camera);
        } finally {
            DisguiseGlowOcclusion.pop();
        }
    }
}
