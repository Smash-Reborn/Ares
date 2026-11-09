package sh.smsh.ares.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.fabric.disguise.DisguiseGlowController;

@Mixin(Entity.class)
public abstract class MixinEntity {

    /** Shared-flag bit the server uses for the glowing outline. */
    private static final int SERVER_GLOW_FLAG = 6;

    @Shadow
    protected abstract boolean getSharedFlag(int flag);

    /**
     * Mod outlines only apply while the server glowing flag is clear.
     * A server outline, such as a hit that forces a disguise white, keeps that glow until the flag drops.
     * Only the mod outline is depth-tested. This flag still opts the entity into glowing.
     */
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void ares$disguiseGlow(final CallbackInfoReturnable<Boolean> cir) {
        if (this.getSharedFlag(SERVER_GLOW_FLAG)) {
            return;
        }
        if (DisguiseGlowController.glowRgb((Entity) (Object) this) != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void ares$disguiseGlowColor(final CallbackInfoReturnable<Integer> cir) {
        if (this.getSharedFlag(SERVER_GLOW_FLAG)) {
            return;
        }
        final Integer color = DisguiseGlowController.glowRgb((Entity) (Object) this);
        if (color != null) {
            cir.setReturnValue(color);
        }
    }
}
