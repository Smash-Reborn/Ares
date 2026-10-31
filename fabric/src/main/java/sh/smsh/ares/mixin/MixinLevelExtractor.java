package sh.smsh.ares.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.fabric.disguise.SelfDisguiseRenderController;

@Mixin(LevelExtractor.class)
public abstract class MixinLevelExtractor {

    @Inject(method = "extractPlayerState", at = @At("HEAD"))
    private void ares$resetPreservePlayerState(
            final Camera camera,
            final DeltaTracker deltaTracker,
            final float partialTick,
            final PlayerRenderState playerRenderState,
            final CallbackInfo ci) {
        SelfDisguiseRenderController.setPreservePlayerState(false);
    }

    @Inject(
            method = "extractPlayerState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
                    shift = At.Shift.BEFORE
            )
    )
    private void ares$preservePlayerState(
            final Camera camera,
            final DeltaTracker deltaTracker,
            final float partialTick,
            final PlayerRenderState playerRenderState,
            final CallbackInfo ci) {
        SelfDisguiseRenderController.setPreservePlayerState(true);
    }

    @Inject(
            method = "extractPlayerState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
                    shift = At.Shift.AFTER
            )
    )
    private void ares$releasePlayerState(
            final Camera camera,
            final DeltaTracker deltaTracker,
            final float partialTick,
            final PlayerRenderState playerRenderState,
            final CallbackInfo ci) {
        SelfDisguiseRenderController.setPreservePlayerState(false);
    }
}
