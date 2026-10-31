package sh.smsh.ares.fabric.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import sh.smsh.ares.config.AresColor;
import sh.smsh.ares.settings.ModSettings;

public final class HighlightColorScreen extends Screen {

    private final Screen parent;

    public HighlightColorScreen(final Screen parent) {
        super(Component.literal("Highlight color"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        final int columns = 4;
        final int buttonWidth = 90;
        final int buttonHeight = 20;
        final int gap = 4;
        final int gridWidth = columns * buttonWidth + (columns - 1) * gap;
        final int left = this.width / 2 - gridWidth / 2;
        int index = 0;

        for (final AresColor color : AresColor.values()) {
            final int column = index % columns;
            final int row = index / columns;
            final AresColor selected = color;
            addRenderableWidget(Button.builder(Component.literal(selected.name()), button -> {
                ModSettings.setHighlightMeleeBoundingBoxColor(selected);
                ModSettings.saveConfig();
                if (this.minecraft != null) {
                    this.minecraft.gui.setScreen(this.parent);
                }
            }).bounds(left + column * (buttonWidth + gap), 40 + row * (buttonHeight + gap), buttonWidth, buttonHeight).build());
            index++;
        }
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(getFont(), this.title, this.width / 2, 16, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}
