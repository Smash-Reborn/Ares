package sh.smsh.ares.forge.gui;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.config.AresColor;

import java.util.function.Consumer;

/**
 * Reusable 8x2 overlay of the 16 vanilla formatting colors.
 * Click a cell to select; click outside the popup to close.
 */
public final class ColorPickerOverlay {

    private static final int COLUMNS = 8;
    private static final int ROWS = 2;
    private static final int CELL_SIZE = 16;
    private static final int CELL_GAP = 4;
    private static final int PADDING = 6;
    private static final int SCREEN_MARGIN = 4;
    private static final int POPUP_WIDTH = PADDING * 2 + COLUMNS * CELL_SIZE + (COLUMNS - 1) * CELL_GAP;
    private static final int POPUP_HEIGHT = PADDING * 2 + ROWS * CELL_SIZE + (ROWS - 1) * CELL_GAP;
    private static final int COLOR_DIM = 0x66000000;
    private static final int COLOR_POPUP_BACKGROUND = 0xE0000000;
    private static final int COLOR_POPUP_BORDER = 0x80FFFFFF;
    private static final int COLOR_CELL_BORDER = 0xFF1A1A1A;
    private static final int COLOR_HOVER_BORDER = 0xFFFFFFFF;
    private static final int COLOR_SELECTED_BORDER = 0xFFFFAA00;

    @Getter private boolean open;
    private int x;
    private int y;
    @Nullable private AresColor selected;
    @Nullable private Consumer<AresColor> onSelect;

    public void open(
            @NotNull final AresColor selected,
            @NotNull final Consumer<AresColor> onSelect,
            final int anchorX,
            final int anchorY,
            final int screenWidth,
            final int screenHeight) {
        this.open = true;
        this.selected = selected;
        this.onSelect = onSelect;
        this.x = anchorX - 4 - POPUP_WIDTH;
        this.y = anchorY - POPUP_HEIGHT / 2;
        clampToScreen(screenWidth, screenHeight);
    }

    public void close() {
        this.open = false;
        this.selected = null;
        this.onSelect = null;
    }

    public void draw(final int mouseX, final int mouseY, final int screenWidth, final int screenHeight) {
        if (!open) {
            return;
        }

        Gui.drawRect(0, 0, screenWidth, screenHeight, COLOR_DIM);
        Gui.drawRect(x, y, x + POPUP_WIDTH, y + POPUP_HEIGHT, COLOR_POPUP_BACKGROUND);
        Gui.drawRect(x, y, x + POPUP_WIDTH, y + 1, COLOR_POPUP_BORDER);
        Gui.drawRect(x, y + POPUP_HEIGHT - 1, x + POPUP_WIDTH, y + POPUP_HEIGHT, COLOR_POPUP_BORDER);
        Gui.drawRect(x, y, x + 1, y + POPUP_HEIGHT, COLOR_POPUP_BORDER);
        Gui.drawRect(x + POPUP_WIDTH - 1, y, x + POPUP_WIDTH, y + POPUP_HEIGHT, COLOR_POPUP_BORDER);

        final AresColor[] colors = AresColor.values();
        for (int i = 0; i < colors.length; i++) {
            final int cellX = getCellX(i);
            final int cellY = getCellY(i);
            final boolean hovered = isInsideCell(mouseX, mouseY, cellX, cellY);
            final boolean isSelected = colors[i] == selected;
            final int borderColor = isSelected
                    ? COLOR_SELECTED_BORDER
                    : (hovered ? COLOR_HOVER_BORDER : COLOR_CELL_BORDER);

            Gui.drawRect(cellX - 1, cellY - 1, cellX + CELL_SIZE + 1, cellY + CELL_SIZE + 1, borderColor);
            Gui.drawRect(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, colors[i].getArgb());
        }
    }

    /**
     * @return {@code true} if the click was inside the popup (and consumed)
     */
    public boolean mouseClicked(final int mouseX, final int mouseY) {
        if (!open) {
            return false;
        }

        if (!isInsidePopup(mouseX, mouseY)) {
            return false;
        }

        final AresColor[] colors = AresColor.values();
        for (int i = 0; i < colors.length; i++) {
            if (!isInsideCell(mouseX, mouseY, getCellX(i), getCellY(i))) {
                continue;
            }

            final Consumer<AresColor> callback = this.onSelect;
            close();
            if (callback != null) {
                callback.accept(colors[i]);
            }
            playClickSound();
            return true;
        }

        return true;
    }

    private void clampToScreen(final int screenWidth, final int screenHeight) {
        if (x < SCREEN_MARGIN) {
            x = SCREEN_MARGIN;
        }
        if (y < SCREEN_MARGIN) {
            y = SCREEN_MARGIN;
        }
        if (x + POPUP_WIDTH > screenWidth - SCREEN_MARGIN) {
            x = screenWidth - SCREEN_MARGIN - POPUP_WIDTH;
        }
        if (y + POPUP_HEIGHT > screenHeight - SCREEN_MARGIN) {
            y = screenHeight - SCREEN_MARGIN - POPUP_HEIGHT;
        }
    }

    private int getCellX(final int index) {
        return x + PADDING + (index % COLUMNS) * (CELL_SIZE + CELL_GAP);
    }

    private int getCellY(final int index) {
        return y + PADDING + (index / COLUMNS) * (CELL_SIZE + CELL_GAP);
    }

    private boolean isInsidePopup(final int mouseX, final int mouseY) {
        return mouseX >= x && mouseX < x + POPUP_WIDTH && mouseY >= y && mouseY < y + POPUP_HEIGHT;
    }

    private static boolean isInsideCell(final int mouseX, final int mouseY, final int cellX, final int cellY) {
        return mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE;
    }

    private static void playClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(
                PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.25F));
    }
}
