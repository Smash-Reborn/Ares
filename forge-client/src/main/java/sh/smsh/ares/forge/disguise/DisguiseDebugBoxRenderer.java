package sh.smsh.ares.forge.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.config.AresColor;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.settings.ModSettings;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseDebugBoxRenderer {

    private static final float PLAYER_WIDTH = 0.6F;
    private static final float PLAYER_HEIGHT = 1.8F;
    private static final double PLAYER_HALF_WIDTH = PLAYER_WIDTH / 2.0D;
    private static final float DEFAULT_WOLF_WIDTH = 0.6F;
    private static final float DEFAULT_WOLF_HEIGHT = 0.8F;
    private static final int COLOR_ALPHA = 255;

    public static void onEntityRendered(
            @Nullable final Entity entity,
            final double x,
            final double y,
            final double z,
            final boolean hideDebugBox,
            final boolean debugBoundingBox) {
        if (hideDebugBox || !debugBoundingBox || entity == null) {
            return;
        }

        if (!DisguiseRegistry.isDisguiseEntity(entity.getEntityId())) {
            return;
        }

        final boolean showHittableNametag = ModSettings.shouldShowHittableNametagBoundingBox();
        final boolean showServersideBox = ModSettings.shouldShowServersideBoundingBox();
        final int wolfEntityId = showHittableNametag
                ? MineplexNametagFixController.getTrackedWolfEntityIdForDisguise(entity.getEntityId())
                : -1;
        final boolean drawWolfBox = wolfEntityId >= 0;

        if (!showServersideBox && !drawWolfBox) {
            return;
        }

        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();

        try {
            if (drawWolfBox) {
                final AresColor hittableColor = ModSettings.getHittableNametagBoundingBoxColor();
                RenderGlobal.drawOutlinedBoundingBox(
                        toWolfBoxOnTopOfDisguise(entity, x, y, z, wolfEntityId),
                        hittableColor.getRed(),
                        hittableColor.getGreen(),
                        hittableColor.getBlue(),
                        COLOR_ALPHA);
            }

            if (showServersideBox) {
                final AresColor serversideColor = ModSettings.getServersideBoundingBoxColor();
                RenderGlobal.drawOutlinedBoundingBox(
                        toPlayerSizedBox(x, y, z),
                        serversideColor.getRed(),
                        serversideColor.getGreen(),
                        serversideColor.getBlue(),
                        COLOR_ALPHA);
            }
        } finally {
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }
    }

    @NotNull
    private static AxisAlignedBB toWolfBoxOnTopOfDisguise(
            @NotNull final Entity disguise,
            final double x,
            final double y,
            final double z,
            final int wolfEntityId) {
        float wolfWidth = DEFAULT_WOLF_WIDTH;
        float wolfHeight = DEFAULT_WOLF_HEIGHT;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null) {
            final Entity wolf = mc.theWorld.getEntityByID(wolfEntityId);
            if (wolf != null) {
                wolfWidth = wolf.width;
                wolfHeight = wolf.height;
            }
        }

        final double halfWidth = wolfWidth / 2.0D;
        final double boxY = y + (double) disguise.height;
        return new AxisAlignedBB(
                x - halfWidth,
                boxY,
                z - halfWidth,
                x + halfWidth,
                boxY + (double) wolfHeight,
                z + halfWidth);
    }

    @NotNull
    private static AxisAlignedBB toPlayerSizedBox(final double x, final double y, final double z) {
        return new AxisAlignedBB(
                x - PLAYER_HALF_WIDTH,
                y,
                z - PLAYER_HALF_WIDTH,
                x + PLAYER_HALF_WIDTH,
                y + (double) PLAYER_HEIGHT,
                z + PLAYER_HALF_WIDTH);
    }
}
