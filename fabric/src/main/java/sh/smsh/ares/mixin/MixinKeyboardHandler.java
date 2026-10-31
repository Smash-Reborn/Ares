package sh.smsh.ares.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sh.smsh.ares.feature.impl.NativeBoundingBoxesFeature;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    /**
     * The first reduced-debug check in {@code handleDebugKeys} is the F3+B hitbox toggle.
     * When native bounding boxes are on, that toggle is allowed anyway.
     */
    @Redirect(
            method = "handleDebugKeys",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isReducedDebugInfo()Z",
                    ordinal = 0
            )
    )
    private boolean ares$allowHitboxToggle(final LocalPlayer player) {
        if (NativeBoundingBoxesFeature.shouldApply()) {
            return false;
        }

        return player.isReducedDebugInfo();
    }
}
