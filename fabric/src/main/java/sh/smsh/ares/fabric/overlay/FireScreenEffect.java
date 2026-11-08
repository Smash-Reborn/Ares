package sh.smsh.ares.fabric.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import sh.smsh.ares.feature.impl.FireOverlayFeature;

/**
 * Vanilla first-person fire, with an optional downward shift and opacity scale.
 * The quad layout matches {@code ScreenEffectRenderer}'s fire submission.
 */
public final class FireScreenEffect {

    private static final float VANILLA_Y = -0.3f;
    private static final int VANILLA_COLOR = 0xE5FFFFFF;

    private FireScreenEffect() {
    }

    public static void submit(
            final PoseStack poseStack,
            final SubmitNodeCollector collector,
            final TextureAtlasSprite sprite) {
        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.fireScreenEffect(sprite.atlasLocation()),
                (pose, consumer) -> render(pose, consumer, sprite));
    }

    private static void render(
            final PoseStack.Pose pose,
            final VertexConsumer consumer,
            final TextureAtlasSprite sprite) {
        final float y = VANILLA_Y - (FireOverlayFeature.shouldApply() ? FireOverlayFeature.getHeightOffset() : 0.0f);
        final int color = FireOverlayFeature.shouldApply()
                ? ARGB.multiplyAlpha(VANILLA_COLOR, FireOverlayFeature.getOpacity())
                : VANILLA_COLOR;

        final Matrix4f matrix = new Matrix4f();
        matrix.set(pose.pose());
        matrix.translate(0.24f, y, 0.0f);
        matrix.rotateY(-0.17453292f);
        buildFireQuad(consumer, matrix, sprite, color);

        matrix.set(pose.pose());
        matrix.translate(-0.24f, y, 0.0f);
        matrix.rotateY(0.17453292f);
        buildFireQuad(consumer, matrix, sprite, color);
    }

    private static void buildFireQuad(
            final VertexConsumer consumer,
            final Matrix4f matrix,
            final TextureAtlasSprite sprite,
            final int color) {
        // UVs are flipped the same way vanilla's fire quad is.
        buildQuad(
                consumer,
                matrix,
                -0.5f,
                -0.5f,
                0.5f,
                0.5f,
                -0.5f,
                sprite.getU1(),
                sprite.getV1(),
                sprite.getU0(),
                sprite.getV0(),
                color);
    }

    private static void buildQuad(
            final VertexConsumer consumer,
            final Matrix4f matrix,
            final float x0,
            final float y0,
            final float x1,
            final float y1,
            final float z,
            final float u0,
            final float v0,
            final float u1,
            final float v1,
            final int color) {
        consumer.addVertex(matrix, x0, y0, z).setUv(u0, v0).setColor(color);
        consumer.addVertex(matrix, x1, y0, z).setUv(u1, v0).setColor(color);
        consumer.addVertex(matrix, x1, y1, z).setUv(u1, v1).setColor(color);
        consumer.addVertex(matrix, x0, y1, z).setUv(u0, v1).setColor(color);
    }
}
