package sh.smsh.ares.forge.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.config.AresColor;
import sh.smsh.ares.connection.ServerType;
import sh.smsh.ares.forge.RenderDistanceApplier;
import sh.smsh.ares.forge.disguise.DisguiseDetectionController;
import sh.smsh.ares.forge.disguise.MineplexNametagFixController;
import sh.smsh.ares.gate.ServerGate;
import sh.smsh.ares.settings.ModSettings;

import java.io.IOException;
import java.util.function.Consumer;

public class AresConfigScreen extends GuiScreen {

    // all buttonIDs must be unique integers
    private static final int BUTTON_ID_DONE = 0;
    private static final int BUTTON_ID_EXTENDED_RENDER_DISTANCE = 1;
    private static final int BUTTON_ID_RENDER_DISTANCE_WEIGHT = 2;
    private static final int BUTTON_ID_ARROW_BOUNCING = 3;
    private static final int BUTTON_ID_ENDERMAN_TELEPORTS = 4;
    private static final int BUTTON_ID_TRACK_DISGUISES = 5;
    private static final int BUTTON_ID_USE_NATIVE_NAMETAG = 6;
    private static final int BUTTON_ID_DISGUISE_NAMETAG_COLOR = 7;
    private static final int BUTTON_ID_HITTABLE_NAMETAG_BOX = 8;
    private static final int BUTTON_ID_HITTABLE_NAMETAG_BOX_COLOR = 9;
    private static final int BUTTON_ID_SERVERSIDE_BOUNDING_BOX = 10;
    private static final int BUTTON_ID_SERVERSIDE_BOUNDING_BOX_COLOR = 11;

    private static final int CONTROL_WIDTH = 150;
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_SPACING = 28;
    private static final int SWATCH_SIZE = 20;
    private static final int LABEL_WIDTH = 220;
    private static final int CONTENT_Y_OFFSET = 8;
    private static final int SETTINGS_PANEL_PADDING = 16;
    private static final int SETTINGS_PANEL_VISUAL_PADDING = 8;
    private static final int SETTINGS_ROW_COUNT = 11;
    private static final int MIN_RENDER_DISTANCE_WEIGHT = 1;
    private static final int MAX_RENDER_DISTANCE_WEIGHT = 16;
    private static final int COLOR_LIGHT_GREY = 0xA0A0A0;
    private static final int COLOR_GREEN = 0x55FF55;
    private static final int COLOR_GOLD = 0xFFAA00;
    private static final int COLOR_RED = 0xFF5555;

    @Nullable private final GuiScreen parentScreen;
    @NotNull private final ColorPickerOverlay colorPicker = new ColorPickerOverlay();
    @Nullable private String statusMessage;

    public AresConfigScreen(@Nullable final GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        colorPicker.close();

        final int x = this.width / 2 + 35;
        final int swatchX = x + CONTROL_WIDTH - SWATCH_SIZE;
        int y = getFirstSettingY();

        this.buttonList.add(new ToggleButton(
                BUTTON_ID_EXTENDED_RENDER_DISTANCE,
                x,
                y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                ModSettings.isExtendedRenderDistanceEnabled()));

        y += CONTROL_SPACING;
        this.buttonList.add(new RenderDistanceSlider(
                BUTTON_ID_RENDER_DISTANCE_WEIGHT,
                x,
                y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                Math.round(ModSettings.getRenderDistanceWeight())));

        y += CONTROL_SPACING;
        this.buttonList.add(new ToggleButton(
                BUTTON_ID_ARROW_BOUNCING,
                x,
                y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                ModSettings.shouldDisableArrowBouncingAnimation()));

        y += CONTROL_SPACING;
        this.buttonList.add(new ToggleButton(
                BUTTON_ID_ENDERMAN_TELEPORTS,
                x,
                y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                ModSettings.shouldDisableRandomEndermanTeleports()));

        y += CONTROL_SPACING;
        this.buttonList.add(new ToggleButton(
                BUTTON_ID_TRACK_DISGUISES,
                x,
                y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                ModSettings.shouldTrackDisguises()));

        y += CONTROL_SPACING;
        this.buttonList.add(new ToggleButton(
                BUTTON_ID_USE_NATIVE_NAMETAG,
                x,
                y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                ModSettings.shouldUseNativeNametag()));

        y += CONTROL_SPACING;
        this.buttonList.add(new ColorSwatchButton(
                BUTTON_ID_DISGUISE_NAMETAG_COLOR,
                swatchX,
                y,
                SWATCH_SIZE,
                SWATCH_SIZE,
                ModSettings.getDisguiseNametagColor()));

        y += CONTROL_SPACING;
        this.buttonList.add(new ToggleButton(
                BUTTON_ID_HITTABLE_NAMETAG_BOX,
                x,
                y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                ModSettings.shouldShowHittableNametagBoundingBox()));

        y += CONTROL_SPACING;
        this.buttonList.add(new ColorSwatchButton(
                BUTTON_ID_HITTABLE_NAMETAG_BOX_COLOR,
                swatchX,
                y,
                SWATCH_SIZE,
                SWATCH_SIZE,
                ModSettings.getHittableNametagBoundingBoxColor()));

        y += CONTROL_SPACING;
        this.buttonList.add(new ToggleButton(
                BUTTON_ID_SERVERSIDE_BOUNDING_BOX,
                x,
                y,
                CONTROL_WIDTH,
                CONTROL_HEIGHT,
                ModSettings.shouldShowServersideBoundingBox()));

        y += CONTROL_SPACING;
        this.buttonList.add(new ColorSwatchButton(
                BUTTON_ID_SERVERSIDE_BOUNDING_BOX_COLOR,
                swatchX,
                y,
                SWATCH_SIZE,
                SWATCH_SIZE,
                ModSettings.getServersideBoundingBoxColor()));

        this.buttonList.add(new GuiButton(
                BUTTON_ID_DONE,
                this.width / 2 - 100,
                getSettingsBottom() + 24,
                200,
                CONTROL_HEIGHT,
                "Done"));
    }

    @Override
    protected void actionPerformed(@NotNull final GuiButton button) {
        switch (button.id) {
            case BUTTON_ID_EXTENDED_RENDER_DISTANCE:
                ModSettings.setExtendedRenderDistanceEnabled(!ModSettings.isExtendedRenderDistanceEnabled());
                ((ToggleButton)button).setStatusEnabled(ModSettings.isExtendedRenderDistanceEnabled());
                RenderDistanceApplier.refreshLoadedEntities();
                break;
            case BUTTON_ID_ARROW_BOUNCING:
                ModSettings.setDisableArrowBouncingAnimation(!ModSettings.shouldDisableArrowBouncingAnimation());
                ((ToggleButton)button).setStatusEnabled(ModSettings.shouldDisableArrowBouncingAnimation());
                break;
            case BUTTON_ID_ENDERMAN_TELEPORTS:
                ModSettings.setDisableRandomEndermanTeleports(!ModSettings.shouldDisableRandomEndermanTeleports());
                ((ToggleButton)button).setStatusEnabled(ModSettings.shouldDisableRandomEndermanTeleports());
                break;
            case BUTTON_ID_TRACK_DISGUISES:
                ModSettings.setTrackDisguises(!ModSettings.shouldTrackDisguises());
                ((ToggleButton)button).setStatusEnabled(ModSettings.shouldTrackDisguises());
                if (!ModSettings.shouldTrackDisguises()) {
                    DisguiseDetectionController.clear();
                }
                break;
            case BUTTON_ID_USE_NATIVE_NAMETAG:
                ModSettings.setUseNativeNametag(!ModSettings.shouldUseNativeNametag());
                ((ToggleButton)button).setStatusEnabled(ModSettings.shouldUseNativeNametag());
                MineplexNametagFixController.onNativeNametagSettingChanged();
                break;
            case BUTTON_ID_DISGUISE_NAMETAG_COLOR:
                openColorPicker((ColorSwatchButton) button, ModSettings::setDisguiseNametagColor);
                break;
            case BUTTON_ID_HITTABLE_NAMETAG_BOX:
                ModSettings.setShowHittableNametagBoundingBox(!ModSettings.shouldShowHittableNametagBoundingBox());
                ((ToggleButton)button).setStatusEnabled(ModSettings.shouldShowHittableNametagBoundingBox());
                break;
            case BUTTON_ID_HITTABLE_NAMETAG_BOX_COLOR:
                openColorPicker((ColorSwatchButton) button, ModSettings::setHittableNametagBoundingBoxColor);
                break;
            case BUTTON_ID_SERVERSIDE_BOUNDING_BOX:
                ModSettings.setShowServersideBoundingBox(!ModSettings.shouldShowServersideBoundingBox());
                ((ToggleButton)button).setStatusEnabled(ModSettings.shouldShowServersideBoundingBox());
                break;
            case BUTTON_ID_SERVERSIDE_BOUNDING_BOX_COLOR:
                openColorPicker((ColorSwatchButton) button, ModSettings::setServersideBoundingBoxColor);
                break;
            case BUTTON_ID_DONE:
                saveSettings();
                this.mc.displayGuiScreen(parentScreen);
                break;
            default:
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        saveSettings();
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        if (colorPicker.isOpen()) {
            if (!colorPicker.mouseClicked(mouseX, mouseY)) {
                colorPicker.close();
            }
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Ares Settings", this.width / 2, getTitleY(), 0xFFFFFF);
        drawServerGateStatus();
        drawSettingsPanel();

        if (statusMessage != null) {
            this.drawCenteredString(this.fontRendererObj, statusMessage, this.width / 2, getSettingsBottom() + 10, COLOR_RED);
        }

        final int buttonMouseX = colorPicker.isOpen() ? -1 : mouseX;
        final int buttonMouseY = colorPicker.isOpen() ? -1 : mouseY;
        super.drawScreen(buttonMouseX, buttonMouseY, partialTicks);
        colorPicker.draw(mouseX, mouseY, this.width, this.height);
    }

    private void openColorPicker(
            @NotNull final ColorSwatchButton button,
            @NotNull final Consumer<AresColor> setter) {
        colorPicker.open(
                button.getColor(),
                color -> {
                    setter.accept(color);
                    button.setColor(color);
                },
                button.xPosition,
                button.yPosition + button.height / 2,
                this.width,
                this.height);
    }

    private void updateRenderDistanceWeight(final int renderDistanceWeight) {
        ModSettings.setRenderDistanceWeight(renderDistanceWeight);
        RenderDistanceApplier.refreshLoadedEntities();
    }

    private void saveSettings() {
        if (!ModSettings.saveConfig()) {
            statusMessage = "Failed to save Ares settings.";
        }
    }

    private void drawServerGateStatus() {
        final ServerType serverType = ServerGate.getCurrentServerType();
        final boolean isKnownServerType = serverType != ServerType.UNKNOWN;
        final String serverTypeText = isKnownServerType ? serverType.getDisplayName() : serverType.name();
        final int firstLineY = getTitleY() + 19;
        final int secondLineY = firstLineY + 12;

        drawCenteredSplitString(
                "ServerGate: ",
                serverTypeText,
                firstLineY,
                COLOR_LIGHT_GREY,
                isKnownServerType ? COLOR_GOLD : COLOR_RED);

        drawCenteredSplitString(
                "Status: ",
                ServerGate.isAllowed() ? "Enabled" : "Disabled",
                secondLineY,
                COLOR_LIGHT_GREY,
                ServerGate.isAllowed() ? COLOR_GREEN : COLOR_RED);
    }

    private void drawSettingsPanel() {
        final int panelLeft = this.width / 2 - LABEL_WIDTH;
        final int panelTop = getSettingsTop() - SETTINGS_PANEL_VISUAL_PADDING;
        final int panelBottom = getSettingsBottom() + SETTINGS_PANEL_VISUAL_PADDING;
        int labelY = getFirstSettingY();

        drawRect(0, panelTop, this.width, panelBottom, 0x99000000);
        drawRect(0, panelTop, this.width, panelTop + 1, 0x80FFFFFF);
        drawRect(0, panelBottom - 1, this.width, panelBottom, 0x80FFFFFF);

        this.drawString(this.fontRendererObj, "Extended Entity Render Distance", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Entity Render Weight", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Disable Arrow Bouncing", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Disable Enderman Teleports", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Track Disguises", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Use Native Nametag", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Disguise Nametag Color", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Show Nametag BoundingBox", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Nametag BoundingBox Color", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Show Serverside BoundingBox", panelLeft, labelY + 6, 0xFFFFFF);
        labelY += CONTROL_SPACING;
        this.drawString(this.fontRendererObj, "Serverside BoundingBox Color", panelLeft, labelY + 6, 0xFFFFFF);
    }

    private void drawCenteredSplitString(
            @NotNull final String label,
            @NotNull final String value,
            final int y,
            final int labelColor,
            final int valueColor) {
        final int totalWidth = this.fontRendererObj.getStringWidth(label) + this.fontRendererObj.getStringWidth(value);
        final int x = this.width / 2 - totalWidth / 2;

        this.drawString(this.fontRendererObj, label, x, y, labelColor);
        this.drawString(this.fontRendererObj, value, x + this.fontRendererObj.getStringWidth(label), y, valueColor);
    }

    private int getTitleY() {
        return 20 + CONTENT_Y_OFFSET;
    }

    private int getSettingsTop() {
        return this.height / 6 + 34 + CONTENT_Y_OFFSET;
    }

    private int getSettingsBottom() {
        return getSettingsTop()
                + SETTINGS_PANEL_PADDING * 2
                + CONTROL_HEIGHT
                + CONTROL_SPACING * (SETTINGS_ROW_COUNT - 1);
    }

    private int getFirstSettingY() {
        return getSettingsTop() + SETTINGS_PANEL_PADDING;
    }

    private class ToggleButton extends GuiButton {

        private boolean statusEnabled;

        ToggleButton(
                final int buttonId,
                final int x,
                final int y,
                final int widthIn,
                final int heightIn,
                final boolean statusEnabled) {
            super(buttonId, x, y, widthIn, heightIn, "");
            setStatusEnabled(statusEnabled);
        }

        void setStatusEnabled(final boolean statusEnabled) {
            this.statusEnabled = statusEnabled;
            this.displayString = statusEnabled ? "Enabled" : "Disabled";
        }

        @Override
        public void drawButton(@NotNull final Minecraft minecraft, final int mouseX, final int mouseY) {
            if (!this.visible) {
                return;
            }

            minecraft.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition
                    && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;

            final int hoverState = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hoverState * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(
                    this.xPosition + this.width / 2,
                    this.yPosition,
                    200 - this.width / 2,
                    46 + hoverState * 20,
                    this.width / 2,
                    this.height);
            this.mouseDragged(minecraft, mouseX, mouseY);
            this.drawCenteredString(
                    minecraft.fontRendererObj,
                    this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + (this.height - 8) / 2,
                    this.statusEnabled ? COLOR_GREEN : COLOR_RED);
        }
    }

    private static class ColorSwatchButton extends GuiButton {

        private static final int COLOR_BORDER = 0xFF1A1A1A;
        private static final int COLOR_HOVER_BORDER = 0xFFFFFFFF;

        @NotNull private AresColor color;

        ColorSwatchButton(
                final int buttonId,
                final int x,
                final int y,
                final int widthIn,
                final int heightIn,
                @NotNull final AresColor color) {
            super(buttonId, x, y, widthIn, heightIn, "");
            this.color = color;
        }

        @NotNull AresColor getColor() {
            return color;
        }

        void setColor(@NotNull final AresColor color) {
            this.color = color;
        }

        @Override
        public void drawButton(@NotNull final Minecraft minecraft, final int mouseX, final int mouseY) {
            if (!this.visible) {
                return;
            }

            this.hovered = mouseX >= this.xPosition
                    && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;

            final int left = this.xPosition;
            final int top = this.yPosition;
            final int right = this.xPosition + this.width;
            final int bottom = this.yPosition + this.height;
            drawRect(left, top, right, bottom, this.hovered ? COLOR_HOVER_BORDER : COLOR_BORDER);
            drawRect(left + 1, top + 1, right - 1, bottom - 1, this.color.getArgb());
        }
    }

    private class RenderDistanceSlider extends GuiButton {

        private int value;
        private boolean dragging;

        RenderDistanceSlider(
                final int buttonId,
                final int x,
                final int y,
                final int widthIn,
                final int heightIn,
                final int currentValue) {
            super(buttonId, x, y, widthIn, heightIn, "");
            this.value = clamp(currentValue);
            updateDisplayString();
        }

        @Override
        public boolean mousePressed(@NotNull final Minecraft minecraft, final int mouseX, final int mouseY) {
            if (!super.mousePressed(minecraft, mouseX, mouseY)) {
                return false;
            }

            this.dragging = true;
            setValueFromMouse(mouseX);
            return true;
        }

        @Override
        public void mouseReleased(final int mouseX, final int mouseY) {
            this.dragging = false;
        }

        @Override
        protected int getHoverState(final boolean mouseOver) {
            return 0;
        }

        @Override
        protected void mouseDragged(@NotNull final Minecraft minecraft, final int mouseX, final int mouseY) {
            if (this.visible && this.dragging) {
                setValueFromMouse(mouseX);
            }

            final int sliderX = this.xPosition + getSliderOffset();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(sliderX, this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(sliderX + 4, this.yPosition, 196, 66, 4, 20);
        }

        private void setValueFromMouse(final int mouseX) {
            final float percent = (float)(mouseX - this.xPosition) / (float)(this.width - 1);
            final int newValue = clamp(Math.round(MIN_RENDER_DISTANCE_WEIGHT
                    + percent * (MAX_RENDER_DISTANCE_WEIGHT - MIN_RENDER_DISTANCE_WEIGHT)));

            if (newValue == this.value) {
                return;
            }

            this.value = newValue;
            updateDisplayString();
            updateRenderDistanceWeight(this.value);
        }

        private void updateDisplayString() {
            this.displayString = String.valueOf(this.value);
        }

        private int getSliderOffset() {
            return Math.round((float)(this.value - MIN_RENDER_DISTANCE_WEIGHT)
                    / (float)(MAX_RENDER_DISTANCE_WEIGHT - MIN_RENDER_DISTANCE_WEIGHT) * (float)(this.width - 8));
        }

        private int clamp(final int rawValue) {
            return Math.max(MIN_RENDER_DISTANCE_WEIGHT, Math.min(MAX_RENDER_DISTANCE_WEIGHT, rawValue));
        }
    }
}
