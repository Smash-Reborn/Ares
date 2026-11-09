package sh.smsh.ares.mixin;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Interface so disguise outline code can read setup and call {@code create}
 * without classloading a mixin class.
 */
@Mixin(RenderType.class)
public interface MixinRenderTypeAccess {

    @Accessor("state")
    RenderSetup ares$state();

    @Invoker("create")
    static RenderType ares$create(final String name, final RenderSetup setup) {
        throw new AssertionError();
    }
}
