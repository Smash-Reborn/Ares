package sh.smsh.ares.mixin;

import net.minecraft.entity.monster.EntityEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.gate.ServerGate;
import sh.smsh.ares.settings.ModSettings;

@Mixin(EntityEnderman.class)
public abstract class MixinEntityEnderman {

    @Inject(method = "teleportRandomly()Z", at = @At("HEAD"), cancellable = true)
    private void ares$disableRandomClientTeleports(final CallbackInfoReturnable<Boolean> cir) {
        if (ServerGate.isAllowed() && ModSettings.shouldDisableRandomEndermanTeleports()) {
            cir.setReturnValue(false);
        }
    }
}
