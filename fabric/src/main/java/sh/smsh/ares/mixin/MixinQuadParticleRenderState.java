package sh.smsh.ares.mixin;

import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import sh.smsh.ares.feature.impl.ParticleOpacityFeature;

@Mixin(QuadParticleRenderState.class)
public abstract class MixinQuadParticleRenderState {

    /**
     * Notes, explosions, flames, crits, and dust are submitted on the opaque particle pipeline,
     * which ignores vertex alpha. Draw them with the matching translucent pipeline so the opacity
     * scale actually blends them. At 100% they stay on the vanilla opaque pipeline.
     */
    @ModifyVariable(method = "add", at = @At("HEAD"), argsOnly = true)
    private SingleQuadParticle.Layer ares$fadeOpaqueLayer(final SingleQuadParticle.Layer layer) {
        if (!ParticleOpacityFeature.shouldApply() || ParticleOpacityFeature.getOpacity() >= 1.0f) {
            return layer;
        }
        if (layer == SingleQuadParticle.Layer.OPAQUE) {
            return SingleQuadParticle.Layer.TRANSLUCENT;
        }
        if (layer == SingleQuadParticle.Layer.OPAQUE_TERRAIN) {
            return SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN;
        }
        if (layer == SingleQuadParticle.Layer.OPAQUE_ITEMS) {
            return SingleQuadParticle.Layer.TRANSLUCENT_ITEMS;
        }
        return layer;
    }

    /**
     * Scales the packed color of every quad particle as it is submitted.
     * Particle fade animations still run; this only multiplies the alpha they already chose.
     */
    @ModifyVariable(method = "add", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int ares$scaleParticleAlpha(final int color) {
        if (!ParticleOpacityFeature.shouldApply()) {
            return color;
        }
        return ARGB.multiplyAlpha(color, ParticleOpacityFeature.getOpacity());
    }
}
