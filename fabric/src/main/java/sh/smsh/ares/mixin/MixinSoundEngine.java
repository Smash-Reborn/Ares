package sh.smsh.ares.mixin;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.smsh.ares.fabric.disguise.DisguiseAmbientSounds;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void ares$cancelDisguiseSound(
            final SoundInstance sound,
            final CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (DisguiseAmbientSounds.shouldCancel(sound.getIdentifier())) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }
}
