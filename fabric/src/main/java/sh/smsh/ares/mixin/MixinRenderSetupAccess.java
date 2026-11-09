package sh.smsh.ares.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RenderSetup.class)
public interface MixinRenderSetupAccess {

    @Accessor("pipeline")
    RenderPipeline ares$pipeline();

    @Accessor("outlineProperty")
    RenderSetup.OutlineProperty ares$outlineProperty();

    @Accessor("outlineTextureName")
    String ares$outlineTextureName();

    @Accessor("textures")
    Map<String, Object> ares$textures();
}
