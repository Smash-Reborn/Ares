package sh.smsh.ares.mixin;

import net.minecraft.entity.monster.EntityEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.feature.impl.RandomEndermanTeleportsDisableFeature;

@Mixin(EntityEnderman.class)
public abstract class MixinEntityEnderman {

    @Inject(method = "teleportRandomly()Z", at = @At("HEAD"), cancellable = true)
    private void ares$disableRandomClientTeleports(final CallbackInfoReturnable<Boolean> cir) {
        if (RandomEndermanTeleportsDisableFeature.shouldDisableTeleports()) {
            cir.setReturnValue(false);
        }
    }
}
