package sh.smsh.ares.forge.gui;

import com.google.common.collect.Ordering;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.feature.impl.DisguiseDetectionFeature;
import sh.smsh.ares.forge.disguise.DisguiseHeadTextures;
import sh.smsh.ares.mixin.AccessorGuiPlayerTabOverlay;
import sh.smsh.ares.settings.ModSettings;

import java.util.List;

/**
 * Vanilla tab list with an extra mob-head slot for tracked disguises:
 * {@code [mob][skin] name} instead of {@code [skin] name}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TabListDisguiseRenderer {

    public static void render(
            @NotNull final GuiPlayerTabOverlay overlay,
            @NotNull final Ordering<NetworkPlayerInfo> ordering,
            final int width,
            @Nullable final Scoreboard scoreboardIn,
            @Nullable final ScoreObjective scoreObjectiveIn) {

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) {
            return;
        }

        final AccessorGuiPlayerTabOverlay accessor = (AccessorGuiPlayerTabOverlay) overlay;
        List<NetworkPlayerInfo> list = ordering.sortedCopy(mc.thePlayer.sendQueue.getPlayerInfoMap());

        int maxNameWidth = 0;
        int maxScoreWidth = 0;

        for (final NetworkPlayerInfo info : list) {
            maxNameWidth = Math.max(maxNameWidth, mc.fontRendererObj.getStringWidth(overlay.getPlayerName(info)));

            if (scoreObjectiveIn != null
                    && scoreboardIn != null
                    && scoreObjectiveIn.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS) {

                final int scoreWidth = mc.fontRendererObj.getStringWidth(
                        " " + scoreboardIn.getValueFromObjective(
                                info.getGameProfile().getName(), scoreObjectiveIn).getScorePoints());
                maxScoreWidth = Math.max(maxScoreWidth, scoreWidth);
            }
        }

        list = list.subList(0, Math.min(list.size(), 80));
        final int playerCount = list.size();
        int rows = playerCount;
        int columns;

        for (columns = 1; rows > 20; rows = (playerCount + columns - 1) / columns) {
            ++columns;
        }

        final boolean showSkins = mc.isIntegratedServerRunning()
                || mc.getNetHandler().getNetworkManager().getIsencrypted();
        final boolean anyDisguiseHeads = showSkins && hasDisguiseInList(list);
        final int headSlotWidth = showSkins ? (anyDisguiseHeads ? 18 : 9) : 0;

        final int scoreSlotWidth;
        if (scoreObjectiveIn != null) {
            if (scoreObjectiveIn.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
                scoreSlotWidth = 90;
            } else {
                scoreSlotWidth = maxScoreWidth;
            }
        } else {
            scoreSlotWidth = 0;
        }

        int columnWidth = Math.min(columns * (headSlotWidth + maxNameWidth + scoreSlotWidth + 13), width - 50) / columns;
        final int listLeft = width / 2 - (columnWidth * columns + (columns - 1) * 5) / 2;
        int y = 10;
        int listWidth = columnWidth * columns + (columns - 1) * 5;

        List<String> headerLines = null;
        List<String> footerLines = null;

        if (accessor.ares$getHeader() != null) {
            headerLines = mc.fontRendererObj.listFormattedStringToWidth(
                    accessor.ares$getHeader().getFormattedText(), width - 50);
            for (final String line : headerLines) {
                listWidth = Math.max(listWidth, mc.fontRendererObj.getStringWidth(line));
            }
        }

        if (accessor.ares$getFooter() != null) {
            footerLines = mc.fontRendererObj.listFormattedStringToWidth(
                    accessor.ares$getFooter().getFormattedText(), width - 50);
            for (final String line : footerLines) {
                listWidth = Math.max(listWidth, mc.fontRendererObj.getStringWidth(line));
            }
        }

        if (headerLines != null) {
            Gui.drawRect(
                    width / 2 - listWidth / 2 - 1,
                    y - 1,
                    width / 2 + listWidth / 2 + 1,
                    y + headerLines.size() * mc.fontRendererObj.FONT_HEIGHT,
                    Integer.MIN_VALUE);

            for (final String line : headerLines) {
                final int lineWidth = mc.fontRendererObj.getStringWidth(line);
                mc.fontRendererObj.drawStringWithShadow(
                        line, (float) (width / 2 - lineWidth / 2), (float) y, -1);
                y += mc.fontRendererObj.FONT_HEIGHT;
            }
            ++y;
        }

        Gui.drawRect(
                width / 2 - listWidth / 2 - 1,
                y - 1,
                width / 2 + listWidth / 2 + 1,
                y + rows * 9,
                Integer.MIN_VALUE);

        for (int index = 0; index < playerCount; ++index) {
            final int column = index / rows;
            final int row = index % rows;
            int x = listLeft + column * columnWidth + column * 5;
            final int rowY = y + row * 9;

            Gui.drawRect(x, rowY, x + columnWidth, rowY + 8, 553648127);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            final NetworkPlayerInfo info = list.get(index);
            String name = overlay.getPlayerName(info);
            final String profileName = info.getGameProfile().getName();
            final DisguiseBinding disguise = resolveDisguiseBinding(profileName);
            final boolean showDisguiseHead = showSkins && disguise != null;

            if (showSkins) {
                final EntityPlayer entityPlayer = mc.theWorld == null
                        ? null
                        : mc.theWorld.getPlayerEntityByUUID(info.getGameProfile().getId());
                final boolean upsideDown = entityPlayer != null
                        && entityPlayer.isWearing(EnumPlayerModelParts.CAPE)
                        && ("Dinnerbone".equals(profileName) || "Grumm".equals(profileName));

                if (showDisguiseHead) {
                    DisguiseHeadTextures.drawHead(disguise.getHeadKey(), x, rowY);
                    x += 9;
                }

                mc.getTextureManager().bindTexture(info.getLocationSkin());
                final int v = 8 + (upsideDown ? 8 : 0);
                final int vHeight = 8 * (upsideDown ? -1 : 1);
                Gui.drawScaledCustomSizeModalRect(x, rowY, 8.0F, (float) v, 8, vHeight, 8, 8, 64.0F, 64.0F);

                if (entityPlayer != null && entityPlayer.isWearing(EnumPlayerModelParts.HAT)) {
                    final int hatV = 8 + (upsideDown ? 8 : 0);
                    final int hatVHeight = 8 * (upsideDown ? -1 : 1);
                    Gui.drawScaledCustomSizeModalRect(
                            x, rowY, 40.0F, (float) hatV, 8, hatVHeight, 8, 8, 64.0F, 64.0F);
                }

                x += 9;
            }

            if (info.getGameType() == WorldSettings.GameType.SPECTATOR) {
                name = EnumChatFormatting.ITALIC + name;
                mc.fontRendererObj.drawStringWithShadow(name, (float) x, (float) rowY, -1862270977);
            } else {
                mc.fontRendererObj.drawStringWithShadow(name, (float) x, (float) rowY, -1);
            }

            if (scoreObjectiveIn != null && info.getGameType() != WorldSettings.GameType.SPECTATOR) {
                final int scoreLeft = x + maxNameWidth + 1;
                final int scoreRight = scoreLeft + scoreSlotWidth;
                if (scoreRight - scoreLeft > 5) {
                    accessor.ares$drawScoreboardValues(
                            scoreObjectiveIn, rowY, profileName, scoreLeft, scoreRight, info);
                }
            }

            final int pingXOffset = showSkins ? (showDisguiseHead ? 18 : 9) : 0;
            accessor.ares$drawPing(columnWidth, x - pingXOffset, rowY, info);
        }

        if (footerLines != null) {
            y = y + rows * 9 + 1;
            Gui.drawRect(
                    width / 2 - listWidth / 2 - 1,
                    y - 1,
                    width / 2 + listWidth / 2 + 1,
                    y + footerLines.size() * mc.fontRendererObj.FONT_HEIGHT,
                    Integer.MIN_VALUE);

            for (final String line : footerLines) {
                final int lineWidth = mc.fontRendererObj.getStringWidth(line);
                mc.fontRendererObj.drawStringWithShadow(
                        line, (float) (width / 2 - lineWidth / 2), (float) y, -1);
                y += mc.fontRendererObj.FONT_HEIGHT;
            }
        }
    }

    private static boolean hasDisguiseInList(@NotNull final List<NetworkPlayerInfo> list) {
        if (!ModSettings.shouldTrackDisguises()) {
            return false;
        }

        for (final NetworkPlayerInfo info : list) {
            if (DisguiseRegistry.getByPlayerName(info.getGameProfile().getName()) != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static DisguiseBinding resolveDisguiseBinding(@NotNull final String profileName) {
        if (!ModSettings.shouldTrackDisguises() || !DisguiseDetectionFeature.shouldApply()) {
            return null;
        }
        return DisguiseRegistry.getByPlayerName(profileName);
    }
}
