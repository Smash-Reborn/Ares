package sh.smsh.ares.fabric.disguise;

import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.AresConstants;
import sh.smsh.ares.mixin.MixinRenderSetupAccess;
import sh.smsh.ares.mixin.MixinRenderTypeAccess;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Disguise glow is a separate outline type that depth-tests against the scene.
 * Vanilla and server outlines keep the see-through pipelines.
 */
public final class DisguiseGlowOcclusion {

    /**
     * 26.3 uses reverse-z, same as terrain: closer fragments have a larger depth.
     * Less-or-equal lets the outline pass behind walls, and the view offset then
     * fails against the model's own depth, which drops the glow on visible mobs.
     */
    private static final DepthStencilState DEPTH = new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false);
    private static final String SAMPLER = "Sampler0";

    private static final Set<EntityRenderState> MARKED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<Object> ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final ArrayDeque<Boolean> CAPTURE = new ArrayDeque<>();
    private static final Map<OutlineKey, RenderType> OUTLINES = new Object2ObjectOpenHashMap<>();

    private static Method textureLocation;
    private static RenderPipeline occludedCull;
    private static RenderPipeline occludedNoCull;

    private DisguiseGlowOcclusion() {
    }

    public static void clearMarks() {
        MARKED.clear();
        ITEMS.clear();
        CAPTURE.clear();
    }

    /** Held-item outline submits created while a disguise model is being submitted. */
    public static void markItem(final Object submit) {
        ITEMS.add(submit);
    }

    public static boolean isItem(final Object submit) {
        return ITEMS.contains(submit);
    }

    public static void mark(final EntityRenderState state) {
        MARKED.add(state);
    }

    public static boolean isMarked(final EntityRenderState state) {
        return MARKED.contains(state);
    }

    public static boolean hasMarks() {
        return !MARKED.isEmpty();
    }

    public static void push(final boolean occlude) {
        CAPTURE.push(occlude);
    }

    public static void pop() {
        CAPTURE.pop();
    }

    public static boolean capturing() {
        return !CAPTURE.isEmpty() && Boolean.TRUE.equals(CAPTURE.peek());
    }

    /**
     * Outline variant for a disguise model part, or null when this render type has no outline.
     */
    @Nullable
    public static RenderType outlineFor(final RenderType source) {
        final RenderSetup setup = ((MixinRenderTypeAccess) (Object) source).ares$state();
        final MixinRenderSetupAccess access = (MixinRenderSetupAccess) (Object) setup;
        if (access.ares$outlineProperty() != RenderSetup.OutlineProperty.AFFECTS_OUTLINE) {
            return null;
        }
        final Identifier texture = texture(access);
        if (texture == null) {
            return null;
        }
        final boolean cull = access.ares$pipeline().isCull();
        return OUTLINES.computeIfAbsent(new OutlineKey(texture, cull), DisguiseGlowOcclusion::createOutline);
    }

    private static RenderType createOutline(final OutlineKey key) {
        final RenderPipeline pipeline = key.cull() ? occludedCull() : occludedNoCull();
        final RenderSetup setup = RenderSetup.builder(pipeline)
                .withTexture(SAMPLER, key.texture())
                .setOutline(RenderSetup.OutlineProperty.IS_OUTLINE)
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .createRenderSetup();
        final String name = "disguise_outline/" + key.texture().getPath() + (key.cull() ? "/cull" : "/no_cull");
        return MixinRenderTypeAccess.ares$create(name, setup);
    }

    @Nullable
    private static Identifier texture(final MixinRenderSetupAccess access) {
        final String name = access.ares$outlineTextureName() != null ? access.ares$outlineTextureName() : SAMPLER;
        Object binding = access.ares$textures().get(name);
        if (binding == null) {
            binding = access.ares$textures().get(SAMPLER);
        }
        if (binding == null) {
            return null;
        }
        try {
            if (textureLocation == null) {
                textureLocation = binding.getClass().getMethod("location");
            }
            return (Identifier) textureLocation.invoke(binding);
        } catch (final ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not read outline texture", exception);
        }
    }

    private static RenderPipeline occludedCull() {
        if (occludedCull == null) {
            occludedCull = copy(RenderPipelines.OUTLINE_CULL, "pipeline/disguise_outline_cull");
        }
        return occludedCull;
    }

    private static RenderPipeline occludedNoCull() {
        if (occludedNoCull == null) {
            occludedNoCull = copy(RenderPipelines.OUTLINE_NO_CULL, "pipeline/disguise_outline_no_cull");
        }
        return occludedNoCull;
    }

    private static RenderPipeline copy(final RenderPipeline source, final String path) {
        final RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(AresConstants.MOD_ID, path))
                .withDepthStencilState(DEPTH)
                .withCull(source.isCull())
                .withPolygonMode(source.getPolygonMode())
                .withPrimitiveTopology(source.getPrimitiveTopology())
                .withPushConstantSize(source.pushConstantSize());
        source.getShaders().forEach((type, shader) -> {
            if (type == ShaderType.VERTEX) {
                builder.withVertexShader(shader);
            } else if (type == ShaderType.FRAGMENT) {
                builder.withFragmentShader(shader);
            }
        });
        source.getBindGroupLayouts().forEach(builder::withBindGroupLayout);
        final var formats = source.getVertexFormatBindings();
        for (int index = 0; index < formats.size(); index++) {
            if (formats.get(index) != null) {
                builder.withVertexBinding(index, formats.get(index));
            }
        }
        final var colors = source.getColorTargetStates();
        for (int index = 0; index < colors.size(); index++) {
            builder.withColorTargetState(index, colors.get(index));
        }
        source.getShaderDefines().flags().forEach(builder::withShaderDefine);
        return builder.build();
    }

    private record OutlineKey(Identifier texture, boolean cull) {
    }
}
