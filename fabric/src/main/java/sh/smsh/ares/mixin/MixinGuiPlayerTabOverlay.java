package sh.smsh.ares.mixin;

import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.gate.ServerGate;

import java.util.List;

@Mixin(GuiPlayerTabOverlay.class)
public abstract class MixinGuiPlayerTabOverlay extends Gui {

    @Unique private static final String ARES_ENABLED_ICON = "\u2714";
    @Unique private static final String ARES_DISABLED_ICON = "\u2716";
    @Unique private static final String ARES_LABEL = "  Ares: ";
    @Unique private static final String ARES_ENABLED_TEXT = "ENABLED";
    @Unique private static final String ARES_DISABLED_TEXT = "DISABLED";
    @Unique private static final int ARES_STATUS_VERTICAL_PADDING = 3;
    @Unique private static final int COLOR_LIGHT_GREY = 0xA0A0A0;
    @Unique private static final int COLOR_GREEN = 0x55FF55;
    @Unique private static final int COLOR_RED = 0xFF5555;

    @Shadow @Final private static Ordering<NetworkPlayerInfo> field_175252_a;

    @Shadow @Final private Minecraft mc;

    @Shadow private IChatComponent header;
    @Shadow private IChatComponent footer;

    @Inject(method = "renderPlayerlist", at = @At("RETURN"))
    private void ares$drawGateStatus(
            final int width,
            final Scoreboard scoreboardIn,
            final ScoreObjective scoreObjectiveIn,
            final CallbackInfo ci) {

        final int y = ares$getTabListBottom(width) + ARES_STATUS_VERTICAL_PADDING;
        final boolean isAllowed = ServerGate.isAllowed();
        final String icon = isAllowed ? ARES_ENABLED_ICON : ARES_DISABLED_ICON;
        final String state = isAllowed ? ARES_ENABLED_TEXT : ARES_DISABLED_TEXT;
        final int stateColor = isAllowed ? COLOR_GREEN : COLOR_RED;
        final int totalWidth = this.mc.fontRendererObj.getStringWidth(icon)
                + this.mc.fontRendererObj.getStringWidth(ARES_LABEL)
                + this.mc.fontRendererObj.getStringWidth(state);

        int x = width / 2 - totalWidth / 2;
        this.mc.fontRendererObj.drawStringWithShadow(icon, x, y, stateColor);
        x += this.mc.fontRendererObj.getStringWidth(icon);
        this.mc.fontRendererObj.drawStringWithShadow(ARES_LABEL, x, y, COLOR_LIGHT_GREY);
        x += this.mc.fontRendererObj.getStringWidth(ARES_LABEL);
        this.mc.fontRendererObj.drawStringWithShadow(state, x, y, stateColor);
    }

    @Unique
    private int ares$getTabListBottom(final int width) {

        final List<NetworkPlayerInfo> players = field_175252_a.sortedCopy(this.mc.thePlayer.sendQueue.getPlayerInfoMap());
        final int playerCount = Math.min(players.size(), 80);
        int rows = playerCount;
        int columns;

        for (columns = 1; rows > 20; rows = (playerCount + columns - 1) / columns) {
            ++columns;
        }

        int top = 10;

        if (this.header != null) {
            top += this.mc.fontRendererObj.listFormattedStringToWidth(
                    this.header.getFormattedText(),
                    width - 50).size() * this.mc.fontRendererObj.FONT_HEIGHT + 1;
        }

        int bottom = top + rows * 9;
        if (this.footer != null) {
            bottom += this.mc.fontRendererObj.listFormattedStringToWidth(
                    this.footer.getFormattedText(),
                    width - 50).size() * this.mc.fontRendererObj.FONT_HEIGHT + 2;
        }

        return bottom;
    }
}
