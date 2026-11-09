package sh.smsh.ares.mixin;

import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import sh.smsh.ares.fabric.disguise.DisguiseGlowOcclusion;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {

    @Shadow
    @Final
    private GameRenderer gameRenderer;

    /**
     * Disguise outlines read this depth buffer. The attachment stays off when no disguise is
     * glowing, and vanilla outline pipelines disable the depth test, so server glow is unchanged.
     */
    @ModifyArg(
            method = "executeOutline",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/renderpearl/api/commands/CommandEncoder;createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/Optional;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/renderpearl/api/commands/RenderPass;"
            ),
            index = 3
    )
    private GpuTextureView ares$outlineSceneDepth(final GpuTextureView depth) {
        if (!DisguiseGlowOcclusion.hasMarks()) {
            return depth;
        }
        return this.gameRenderer.mainRenderTarget().getDepthTextureView();
    }
}
