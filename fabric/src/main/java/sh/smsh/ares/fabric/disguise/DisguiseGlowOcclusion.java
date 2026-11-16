package sh.smsh.ares.fabric.disguise;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseGlowOcclusion {

    /**
     * 26.3 uses reverse-z, same as terrain: closer fragments have a larger depth.
     * Less-or-equal lets the outline pass behind walls, and the view offset then
     * fails against the model's own depth, which drops the glow on visible mobs.
     */
    private static final DepthStencilState DEPTH = new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false);
    private static final String SAMPLER = "Sampler0";
    /**
     * Mooshroom back mushrooms are full-block cross planes. At full size their near
     * edge sits on the camera side of a wall that already covers the cow. Shrink the
     * outline around the block center so that edge stays with the body.
     */
    private static final float BLOCK_OUTLINE_SCALE = 0.55f;
    /** View-ray push, in blocks, applied by the block-outline vertex shader. */
    private static final float BLOCK_OUTLINE_PUSH = 0.32f;
    private static final Matrix4fc BLOCK_INSET = new Matrix4f()
            .translate(0.5f, 0.5f, 0.5f)
            .scale(BLOCK_OUTLINE_SCALE)
            .translate(-0.5f, -0.5f, -0.5f);
    private static final Identifier BLOCK_OUTLINE_SHADER = Identifier.fromNamespaceAndPath(
            AresConstants.MOD_ID, "core/disguise_block_outline");

    private static final Set<EntityRenderState> MARKED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<Object> ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<Object> BLOCK_MODELS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<RenderType> OURS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<RenderType> BLOCK_OUTLINE_TYPES = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<RenderType> BLOCK_SOLID_TYPES = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final ArrayDeque<Boolean> CAPTURE = new ArrayDeque<>();
    private static final Map<OutlineKey, RenderType> OUTLINES = new Object2ObjectOpenHashMap<>();
    private static final Map<Identifier, RenderType> BLOCK_OUTLINES = new Object2ObjectOpenHashMap<>();
    private static final Map<RenderType, RenderType> BLOCK_SOLIDS = new IdentityHashMap<>();

    private static Method textureLocation;
    private static RenderPipeline occludedCull;
    private static RenderPipeline occludedNoCull;
    private static RenderPipeline blockOutlinePipeline;
    private static int blockSolidSerial;

    public static void clearMarks() {
        MARKED.clear();
        ITEMS.clear();
        BLOCK_MODELS.clear();
        CAPTURE.clear();
    }

    /** Held-item outline submits created while a disguise model is being submitted. */
    public static void markItem(final Object submit) {
        ITEMS.add(submit);
    }

    public static boolean isItem(final Object submit) {
        return ITEMS.contains(submit);
    }

    /**
     * Block-model submits created while a disguise is being submitted.
     * Mooshroom mushrooms and carried blocks keep a vanilla outline unless these are swapped later.
     */
    public static void markBlockModel(final Object submit) {
        BLOCK_MODELS.add(submit);
    }

    public static boolean isBlockModel(final Object submit) {
        return BLOCK_MODELS.contains(submit);
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
     * Depth-tested outline for a model part, a block-model sheet, or a vanilla outline type.
     * Returns null when this render type has no outline texture.
     */
    @Nullable
    public static RenderType outlineFor(final RenderType source) {
        if (OURS.contains(source)) {
            return source;
        }
        final RenderSetup setup = ((MixinRenderTypeAccess) (Object) source).ares$state();
        final MixinRenderSetupAccess access = (MixinRenderSetupAccess) (Object) setup;
        final RenderSetup.OutlineProperty property = access.ares$outlineProperty();
        if (property != RenderSetup.OutlineProperty.AFFECTS_OUTLINE
                && property != RenderSetup.OutlineProperty.IS_OUTLINE) {
            return null;
        }
        final Identifier texture = texture(access);
        if (texture == null) {
            return null;
        }
        final boolean cull = access.ares$pipeline().isCull();
        return OUTLINES.computeIfAbsent(new OutlineKey(texture, cull), DisguiseGlowOcclusion::createOutline);
    }

    /**
     * Block-sprite outline used for mooshroom mushrooms and carried blocks.
     * No view offset, both sides of a cross, and a view-ray push so the glow
     * stays behind a wall that covers the body.
     */
    @Nullable
    public static RenderType blockOutlineFor(final RenderType source) {
        if (source != null && BLOCK_OUTLINE_TYPES.contains(source)) {
            return source;
        }
        final Identifier texture = textureOf(source);
        if (texture == null) {
            return null;
        }
        return BLOCK_OUTLINES.computeIfAbsent(texture, DisguiseGlowOcclusion::createBlockOutline);
    }

    /**
     * Same cutout as the block sprite, but it does not write depth. The outline is
     * pushed behind that sprite, and the sprite's own depth would otherwise erase it.
     */
    public static RenderType blockSolidFor(final RenderType source) {
        if (source == null || BLOCK_SOLID_TYPES.contains(source)) {
            return source;
        }
        final Identifier texture = textureOf(source);
        if (texture == null) {
            return source;
        }
        return BLOCK_SOLIDS.computeIfAbsent(source, type -> createBlockSolid(type, texture));
    }

    /** Shrink a block outline around the block center. The solid sprite keeps its pose. */
    public static PoseStack.Pose insetBlockOutline(final PoseStack.Pose pose) {
        final PoseStack.Pose copy = pose.copy();
        copy.mulPose(BLOCK_INSET);
        return copy;
    }

    private static RenderType createOutline(final OutlineKey key) {
        final RenderPipeline pipeline = key.cull() ? occludedCull() : occludedNoCull();
        final RenderSetup setup = RenderSetup.builder(pipeline)
                .withTexture(SAMPLER, key.texture())
                .setOutline(RenderSetup.OutlineProperty.IS_OUTLINE)
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .createRenderSetup();
        final String name = "disguise_outline/" + key.texture().getPath() + (key.cull() ? "/cull" : "/no_cull");
        final RenderType type = MixinRenderTypeAccess.ares$create(name, setup);
        OURS.add(type);
        return type;
    }

    private static RenderType createBlockOutline(final Identifier texture) {
        final RenderSetup setup = RenderSetup.builder(blockOutlinePipeline())
                .withTexture(SAMPLER, texture)
                .setOutline(RenderSetup.OutlineProperty.IS_OUTLINE)
                .setLayeringTransform(LayeringTransform.NO_LAYERING)
                .createRenderSetup();
        final RenderType type = MixinRenderTypeAccess.ares$create(
                "disguise_block_outline/" + texture.getPath(), setup);
        OURS.add(type);
        BLOCK_OUTLINE_TYPES.add(type);
        return type;
    }

    private static RenderType createBlockSolid(final RenderType source, final Identifier texture) {
        final RenderPipeline pipeline = copy(
                setup(source).ares$pipeline(),
                "pipeline/disguise_block_solid/" + blockSolidSerial++);
        final RenderSetup setup = RenderSetup.builder(pipeline)
                .withTexture(SAMPLER, texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        final RenderType type = MixinRenderTypeAccess.ares$create(
                "disguise_block_solid/" + texture.getPath() + "/" + BLOCK_SOLID_TYPES.size(), setup);
        BLOCK_SOLID_TYPES.add(type);
        return type;
    }

    @Nullable
    private static Identifier textureOf(final RenderType source) {
        if (source == null) {
            return null;
        }
        return texture(setup(source));
    }

    private static MixinRenderSetupAccess setup(final RenderType source) {
        return (MixinRenderSetupAccess) (Object) ((MixinRenderTypeAccess) (Object) source).ares$state();
    }

    @Nullable
    private static Identifier texture(final MixinRenderSetupAccess access) {
        final String name = access.ares$outlineTextureName() != null ? access.ares$outlineTextureName() : SAMPLER;
        Object binding = access.ares$textures().get(name);
        if (binding == null) {
            binding = access.ares$textures().get(SAMPLER);
        }
        if (binding == null && !access.ares$textures().isEmpty()) {
            binding = access.ares$textures().values().iterator().next();
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

    private static RenderPipeline blockOutlinePipeline() {
        if (blockOutlinePipeline == null) {
            final RenderPipeline.Builder builder = builderFrom(
                    RenderPipelines.OUTLINE_NO_CULL,
                    Identifier.fromNamespaceAndPath(AresConstants.MOD_ID, "pipeline/disguise_block_outline"));
            builder.withVertexShader(BLOCK_OUTLINE_SHADER);
            builder.withShaderDefine("DISGUISE_BLOCK_PUSH", BLOCK_OUTLINE_PUSH);
            blockOutlinePipeline = builder.build();
        }
        return blockOutlinePipeline;
    }

    private static RenderPipeline copy(final RenderPipeline source, final String path) {
        return builderFrom(source, Identifier.fromNamespaceAndPath(AresConstants.MOD_ID, path)).build();
    }

    private static RenderPipeline.Builder builderFrom(final RenderPipeline source, final Identifier location) {
        final RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(location)
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
        return builder;
    }

    private record OutlineKey(Identifier texture, boolean cull) {
    }
}
