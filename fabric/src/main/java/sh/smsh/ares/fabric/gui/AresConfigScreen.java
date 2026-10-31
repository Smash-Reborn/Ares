package sh.smsh.ares.fabric.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.config.AresColor;
import sh.smsh.ares.connection.ServerType;
import sh.smsh.ares.gate.ServerGate;
import sh.smsh.ares.settings.ModSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AresConfigScreen extends Screen {

    private static final int CONTROL_WIDTH = 150;
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_SPACING = 33;
    private static final int LABEL_COLUMN = 235;
    private static final int SETTINGS_PANEL_PADDING = 24;
    private static final int SETTINGS_PANEL_VISUAL_PADDING = 8;
    private static final int MAX_VISIBLE_ROWS = 8;
    private static final int MIN_RENDER_DISTANCE_WEIGHT = 1;
    private static final int MAX_RENDER_DISTANCE_WEIGHT = 16;
    private static final int SWATCH_SIZE = 18;
    private static final int PALETTE_COLUMNS = 8;
    private static final int PALETTE_CELL = 16;
    private static final int PALETTE_GAP = 4;
    private static final int PALETTE_PAD = 4;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int COLOR_LIGHT_GREY = 0xFFA0A0A0;
    private static final int COLOR_GREEN = 0xFF55FF55;
    private static final int COLOR_GOLD = 0xFFFFAA00;
    private static final int COLOR_RED = 0xFFFF5555;

    @Nullable private final Screen parent;
    private final List<SettingRow> rows = new ArrayList<>();
    @Nullable private ColorSwatch openSwatch;
    private int scrollOffset;
    private boolean draggingScroll;
    @Nullable private String statusMessage;

    public AresConfigScreen(@Nullable final Screen parent) {
        super(Component.literal("Ares Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.openSwatch = null;
        this.rows.clear();

        addToggle("Extended Entity Render",
                "Determines whether to forcibly render all entities from further away via the render weight value. Very useful for seeing entities that de-spawn on clients from far distances, such as item_tile, animals, etc.",
                ModSettings.isExtendedRenderDistanceEnabled(), () -> {
            ModSettings.setExtendedRenderDistanceEnabled(!ModSettings.isExtendedRenderDistanceEnabled());
            return ModSettings.isExtendedRenderDistanceEnabled();
        });
        addSlider(Math.round(ModSettings.getRenderDistanceWeight()));
        addToggle("Disable Arrow Bouncing",
                "Client won't render the arrow bouncing animation, meaning the projectile will continue to travel on its normal trajectory, behaving like other projectiles. (eggs, snowballs, etc)",
                ModSettings.shouldDisableArrowBouncingAnimation(), () -> {
            ModSettings.setDisableArrowBouncingAnimation(!ModSettings.shouldDisableArrowBouncingAnimation());
            return ModSettings.shouldDisableArrowBouncingAnimation();
        });
        addToggle("Track Disguises",
                "Tracks clientside disguises sent from the server, which enables various other functionality such as the bounding boxes, self disguise, tab-bar head renders, etc.",
                ModSettings.shouldTrackDisguises(), () -> {
            ModSettings.setTrackDisguises(!ModSettings.shouldTrackDisguises());
            return ModSettings.shouldTrackDisguises();
        });
        addToggle("Render Self Disguise",
                "While in 3rd person view, you can see what mob you are disguised as. This replaced the default player model.",
                ModSettings.shouldShowSelfDisguiseEntity(), () -> {
            ModSettings.setShowSelfDisguiseEntity(!ModSettings.shouldShowSelfDisguiseEntity());
            return ModSettings.shouldShowSelfDisguiseEntity();
        });
        addToggle("Native Bounding Boxes",
                "Restores 1.8 bounding box visual functionality, so you can see all entity bounding boxes. Disguise-based bounding boxes are tracked individually to allow for extra functionality.",
                ModSettings.shouldForceNativeBoundingBoxes(), () -> {
            ModSettings.setForceNativeBoundingBoxes(!ModSettings.shouldForceNativeBoundingBoxes());
            this.minecraft.debugEntries.rebuildCurrentList();
            return ModSettings.shouldForceNativeBoundingBoxes();
        });
        addToggle("Highlight on Bounding Box",
                "If you are within melee-attacking distance of a disguise-based bounding box, the box color will highlight to the selected color-code. This does NOT mean your melee hit will succeed, only that your client will send an attacking packet, server still does its own calculation.",
                ModSettings.shouldHighlightMeleeBoundingBox(), () -> {
            ModSettings.setHighlightMeleeBoundingBox(!ModSettings.shouldHighlightMeleeBoundingBox());
            return ModSettings.shouldHighlightMeleeBoundingBox();
        });
        addColorSwatch();
        addToggle("Dynamic Melee Bounding Box",
                "Tools now provide a specialized item component which grows the hitbox_margin of a targets bounding box by 0.1, mimicing 1.8s clientside melee bounding box growth. If enabled, this feature makes the \"highlighting\" of the bounding box respect the new margin.",
                ModSettings.shouldDynamicallyScaleDisguiseBoundingBoxes(), () -> {
            ModSettings.setDynamicallyScaleDisguiseBoundingBoxes(!ModSettings.shouldDynamicallyScaleDisguiseBoundingBoxes());
            return ModSettings.shouldDynamicallyScaleDisguiseBoundingBoxes();
        });
        addToggle("Legacy Damage Tilt",
                "In 1.8, the damage tilt animation was bugged and would always render with a yaw of 0f. Modern versions have a variable yaw which is calculated based on damage direction. If enabled, restores the legacy 0f yaw just like in 1.8.",
                ModSettings.isLegacyDamageTilt(), () -> {
            ModSettings.setLegacyDamageTilt(!ModSettings.isLegacyDamageTilt());
            return ModSettings.isLegacyDamageTilt();
        });
        addToggle("Legacy Damage Sound",
                "In 1.8, when taking damage, both the entity sound and a generic hurt sound would play, to distinctively indicate to the player that they were being damaged. If enabled, this restores that functionality. NOTE: \"Disable Ambient Disguise Sounds\" must be true in order for this to work, as the passive sounds are linked to the hurt sounds.",
                ModSettings.isLegacyDamageSound(), () -> {
            ModSettings.setLegacyDamageSound(!ModSettings.isLegacyDamageSound());
            return ModSettings.isLegacyDamageSound();
        });
        addToggle("Disable Damage Indicators",
                "When enabled, you will not render any text display entities which are used as damage indicators.",
                ModSettings.isDisableDamageIndicators(), () -> {
            ModSettings.setDisableDamageIndicators(!ModSettings.isDisableDamageIndicators());
            return ModSettings.isDisableDamageIndicators();
        });
        addToggle("Disable Ambient Disguise Sounds",
                "When enabled, your client will not play any ambient hurt, death, step, walk, idle, etc sound effects.",
                ModSettings.isDisableAmbientDisguiseSounds(), () -> {
            ModSettings.setDisableAmbientDisguiseSounds(!ModSettings.isDisableAmbientDisguiseSounds());
            return ModSettings.isDisableAmbientDisguiseSounds();
        });

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(this.width / 2 - 100, getSettingsBottom() + 24, 200, CONTROL_HEIGHT)
                .build());
        applyScroll();
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        graphics.centeredText(getFont(), this.title, this.width / 2, getTitleY(), 0xFFFFFFFF);
        drawServerGateStatus(graphics);
        drawSettingsPanel(graphics);

        final int clipTop = panelTop() + 1;
        final int clipBottom = panelBottom() - 1;
        graphics.enableScissor(0, clipTop, this.width, clipBottom);
        @Nullable String hoveredDescription = null;
        for (final SettingRow row : this.rows) {
            if (!row.widget.visible) {
                continue;
            }
            final int textX = labelX();
            final int textY = row.widget.getY() + 6 - row.yOffset;
            graphics.text(getFont(), row.label, textX, textY, 0xFFFFFFFF);
            if (row.description != null
                    && mouseX >= textX
                    && mouseX < textX + getFont().width(row.label)
                    && mouseY >= textY
                    && mouseY < textY + getFont().lineHeight
                    && mouseY >= clipTop
                    && mouseY < clipBottom) {
                hoveredDescription = row.description;
            }
            row.widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
        graphics.disableScissor();

        if (hoveredDescription != null) {
            graphics.setTooltipForNextFrame(
                    getFont(),
                    getFont().split(Component.literal(hoveredDescription), 220),
                    mouseX,
                    mouseY);
        }

        drawScrollbar(graphics);
        drawPalette(graphics);
        for (final var child : children()) {
            if (child instanceof AbstractWidget widget && !this.rows.stream().anyMatch(row -> row.widget == widget)) {
                widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }

        if (this.statusMessage != null) {
            graphics.centeredText(getFont(), this.statusMessage, this.width / 2, getSettingsBottom() + 10, COLOR_RED);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubled) {
        if (this.openSwatch != null) {
            final AresColor picked = colorAt(event.x(), event.y());
            if (picked != null) {
                ModSettings.setHighlightMeleeBoundingBoxColor(picked);
                this.openSwatch.color = picked;
                this.openSwatch = null;
                return true;
            }

            if (!this.openSwatch.isMouseOver(event.x(), event.y())) {
                this.openSwatch = null;
                return true;
            }
        }

        if (maxScroll() > 0 && overScrollbar(event.x(), event.y())) {
            this.draggingScroll = true;
            setScrollFromMouse(event.y());
            return true;
        }

        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double dragX, final double dragY) {
        if (this.draggingScroll) {
            setScrollFromMouse(event.y());
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        this.draggingScroll = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        if (maxScroll() > 0 && mouseY >= panelTop() && mouseY < panelBottom()) {
            int delta = (int) Math.round(scrollY * CONTROL_SPACING);
            if (delta == 0) {
                delta = scrollY > 0 ? CONTROL_SPACING : -CONTROL_SPACING;
            }
            setScroll(this.scrollOffset - delta);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        if (!ModSettings.saveConfig()) {
            this.statusMessage = "Failed to save Ares settings.";
        }
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    private void addToggle(
            final String label,
            @Nullable final String description,
            final boolean enabled,
            final ToggleAction action) {
        final int index = this.rows.size();
        final StatusButton button = new StatusButton(controlX(), 0, statusText(label, enabled), pressed ->
                pressed.setMessage(statusText(label, action.toggle())));
        addRenderableWidget(button);
        this.rows.add(new SettingRow(label, description, button, 0, index));
    }

    private void addSlider(final int weight) {
        final int index = this.rows.size();
        final WeightSlider slider = new WeightSlider(controlX(), 0, weight);
        addRenderableWidget(slider);
        this.rows.add(new SettingRow("Entity Render Weight", null, slider, 0, index));
    }

    private void addColorSwatch() {
        final int index = this.rows.size();
        final ColorSwatch swatch = new ColorSwatch(
                controlX() + CONTROL_WIDTH - SWATCH_SIZE,
                0,
                ModSettings.getHighlightMeleeBoundingBoxColor());
        addRenderableWidget(swatch);
        this.rows.add(new SettingRow("Highlight color", null, swatch, 1, index));
    }

    private void applyScroll() {
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll()));
        final int firstY = getFirstSettingY();
        for (final SettingRow row : this.rows) {
            final int y = firstY + row.index * CONTROL_SPACING - this.scrollOffset + row.yOffset;
            row.widget.setY(y);
            final int top = y - row.yOffset;
            row.widget.visible = top + CONTROL_HEIGHT > panelTop() && top < panelBottom();
        }

        if (this.openSwatch != null && !this.openSwatch.visible) {
            this.openSwatch = null;
        }
    }

    private void setScroll(final int offset) {
        this.scrollOffset = offset;
        applyScroll();
    }

    private void setScrollFromMouse(final double mouseY) {
        final int trackTop = panelTop() + 1;
        final int trackHeight = Math.max(1, panelBottom() - panelTop() - 2);
        final int handle = handleHeight(trackHeight);
        final double span = Math.max(1, trackHeight - handle);
        final double percent = (mouseY - trackTop - handle / 2.0) / span;
        setScroll((int) Math.round(Math.max(0, Math.min(1, percent)) * maxScroll()));
    }

    private void drawServerGateStatus(final GuiGraphicsExtractor graphics) {
        final ServerType serverType = ServerGate.getCurrentServerType();
        final boolean known = serverType != ServerType.UNKNOWN;
        final int firstLineY = getTitleY() + 19;
        drawCenteredSplit(
                graphics,
                "ServerGate: ",
                known ? serverType.getDisplayName() : serverType.name(),
                firstLineY,
                COLOR_LIGHT_GREY,
                known ? COLOR_GOLD : COLOR_RED);
        drawCenteredSplit(
                graphics,
                "Status: ",
                ServerGate.isAllowed() ? "Enabled" : "Disabled",
                firstLineY + 12,
                COLOR_LIGHT_GREY,
                ServerGate.isAllowed() ? COLOR_GREEN : COLOR_RED);
    }

    private void drawSettingsPanel(final GuiGraphicsExtractor graphics) {
        final int top = panelTop();
        final int bottom = panelBottom();
        graphics.fill(0, top, this.width, bottom, 0x99000000);
        graphics.fill(0, top, this.width, top + 1, 0x80FFFFFF);
        graphics.fill(0, bottom - 1, this.width, bottom, 0x80FFFFFF);
    }

    private void drawScrollbar(final GuiGraphicsExtractor graphics) {
        if (maxScroll() <= 0) {
            return;
        }

        final int x = this.width - SCROLLBAR_WIDTH - 2;
        final int top = panelTop() + 1;
        final int bottom = panelBottom() - 1;
        final int trackHeight = bottom - top;
        final int handle = handleHeight(trackHeight);
        final int handleY = top + (trackHeight - handle) * this.scrollOffset / maxScroll();
        graphics.fill(x, top, x + SCROLLBAR_WIDTH, bottom, 0x80000000);
        graphics.fill(x, handleY, x + SCROLLBAR_WIDTH, handleY + handle, 0xFFC0C0C0);
    }

    private void drawPalette(final GuiGraphicsExtractor graphics) {
        if (this.openSwatch == null) {
            return;
        }

        final int x = paletteX();
        final int y = paletteY();
        graphics.fill(x, y, x + paletteWidth(), y + paletteHeight(), 0xFF000000);
        int index = 0;
        for (final AresColor color : AresColor.values()) {
            final int cellX = x + PALETTE_PAD + (index % PALETTE_COLUMNS) * (PALETTE_CELL + PALETTE_GAP);
            final int cellY = y + PALETTE_PAD + (index / PALETTE_COLUMNS) * (PALETTE_CELL + PALETTE_GAP);
            graphics.fill(cellX, cellY, cellX + PALETTE_CELL, cellY + PALETTE_CELL, 0xFF202020);
            graphics.fill(cellX + 1, cellY + 1, cellX + PALETTE_CELL - 1, cellY + PALETTE_CELL - 1, color.getArgb());
            index++;
        }
    }

    private void drawCenteredSplit(
            final GuiGraphicsExtractor graphics,
            final String label,
            final String value,
            final int y,
            final int labelColor,
            final int valueColor) {
        final int labelWidth = getFont().width(label);
        final int x = this.width / 2 - (labelWidth + getFont().width(value)) / 2;
        graphics.text(getFont(), label, x, y, labelColor);
        graphics.text(getFont(), value, x + labelWidth, y, valueColor);
    }

    @Nullable
    private AresColor colorAt(final double mouseX, final double mouseY) {
        if (this.openSwatch == null) {
            return null;
        }

        final int localX = (int) mouseX - paletteX() - PALETTE_PAD;
        final int localY = (int) mouseY - paletteY() - PALETTE_PAD;
        if (localX < 0 || localY < 0) {
            return null;
        }

        final int column = localX / (PALETTE_CELL + PALETTE_GAP);
        final int row = localY / (PALETTE_CELL + PALETTE_GAP);
        if (column >= PALETTE_COLUMNS || row >= 2) {
            return null;
        }
        if (localX % (PALETTE_CELL + PALETTE_GAP) >= PALETTE_CELL || localY % (PALETTE_CELL + PALETTE_GAP) >= PALETTE_CELL) {
            return null;
        }

        final int index = row * PALETTE_COLUMNS + column;
        final AresColor[] colors = AresColor.values();
        return index < colors.length ? colors[index] : null;
    }

    private boolean overScrollbar(final double mouseX, final double mouseY) {
        final int x = this.width - SCROLLBAR_WIDTH - 2;
        return mouseX >= x && mouseX < x + SCROLLBAR_WIDTH && mouseY >= panelTop() && mouseY < panelBottom();
    }

    private boolean inView(final double mouseY) {
        return mouseY >= panelTop() + 1 && mouseY < panelBottom() - 1;
    }

    private int paletteX() {
        return this.openSwatch.getX() - 6 - paletteWidth();
    }

    private int paletteY() {
        final int preferred = this.openSwatch.getY() + SWATCH_SIZE / 2 - paletteHeight() / 2;
        final int min = panelTop() + 2;
        final int max = panelBottom() - paletteHeight() - 2;
        return Math.max(min, Math.min(max, preferred));
    }

    private int paletteWidth() {
        return PALETTE_PAD * 2 + PALETTE_COLUMNS * PALETTE_CELL + (PALETTE_COLUMNS - 1) * PALETTE_GAP;
    }

    private int paletteHeight() {
        return PALETTE_PAD * 2 + 2 * PALETTE_CELL + PALETTE_GAP;
    }

    private int handleHeight(final int trackHeight) {
        final int visible = visibleRowCount();
        return Math.max(CONTROL_HEIGHT, trackHeight * visible / this.rows.size());
    }

    private int maxScroll() {
        return Math.max(0, (this.rows.size() - visibleRowCount()) * CONTROL_SPACING);
    }

    private int visibleRowCount() {
        final int room = this.height - 48 - getSettingsTop() - SETTINGS_PANEL_PADDING * 2 - CONTROL_HEIGHT;
        final int fit = 1 + Math.max(0, room / CONTROL_SPACING);
        return Math.max(1, Math.min(MAX_VISIBLE_ROWS, Math.min(fit, Math.max(1, this.rows.size()))));
    }

    private int getTitleY() {
        return 32;
    }

    private int getSettingsTop() {
        return this.height / 6 + 40;
    }

    private int getFirstSettingY() {
        return getSettingsTop() + SETTINGS_PANEL_PADDING;
    }

    private int getSettingsBottom() {
        return getFirstSettingY()
                + SETTINGS_PANEL_PADDING
                + CONTROL_HEIGHT
                + CONTROL_SPACING * (visibleRowCount() - 1);
    }

    private int panelTop() {
        return getSettingsTop() - SETTINGS_PANEL_VISUAL_PADDING;
    }

    private int panelBottom() {
        return getSettingsBottom() + SETTINGS_PANEL_VISUAL_PADDING;
    }

    private int labelX() {
        return this.width / 2 - LABEL_COLUMN;
    }

    private int controlX() {
        return this.width / 2 + 35;
    }

    private static Component statusText(final String label, final boolean enabled) {
        final String lower = label.toLowerCase(Locale.ROOT);
        final boolean truthLabel = lower.startsWith("enable") || lower.startsWith("disable");
        final String word = truthLabel
                ? (enabled ? "True" : "False")
                : (enabled ? "Enabled" : "Disabled");
        return Component.literal(word).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    @FunctionalInterface
    private interface ToggleAction {
        boolean toggle();
    }

    private record SettingRow(@NotNull String label, @Nullable String description, AbstractWidget widget, int yOffset, int index) {
    }

    private final class StatusButton extends Button.Plain {

        private StatusButton(final int x, final int y, final Component message, final OnPress onPress) {
            super(x, y, CONTROL_WIDTH, CONTROL_HEIGHT, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        public boolean isMouseOver(final double mouseX, final double mouseY) {
            return this.visible && inView(mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }

    private final class WeightSlider extends AbstractSliderButton {

        private WeightSlider(final int x, final int y, final int weight) {
            super(x, y, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty(),
                    (weight - MIN_RENDER_DISTANCE_WEIGHT) / (double) (MAX_RENDER_DISTANCE_WEIGHT - MIN_RENDER_DISTANCE_WEIGHT));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(Integer.toString(currentWeight())));
        }

        @Override
        protected void applyValue() {
            ModSettings.setRenderDistanceWeight(currentWeight());
        }

        @Override
        public boolean isMouseOver(final double mouseX, final double mouseY) {
            return this.visible && inView(mouseY) && super.isMouseOver(mouseX, mouseY);
        }

        private int currentWeight() {
            return MIN_RENDER_DISTANCE_WEIGHT + (int) Math.round(this.value * (MAX_RENDER_DISTANCE_WEIGHT - MIN_RENDER_DISTANCE_WEIGHT));
        }
    }

    private final class ColorSwatch extends AbstractWidget {

        private AresColor color;

        private ColorSwatch(final int x, final int y, final AresColor color) {
            super(x, y, SWATCH_SIZE, SWATCH_SIZE, Component.empty());
            this.color = color;
        }

        @Override
        protected void extractWidgetRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
            graphics.fill(getX(), getY(), getRight(), getBottom(), 0xFF000000);
            graphics.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, this.color.getArgb());
        }

        @Override
        public void onClick(final MouseButtonEvent event, final boolean doubled) {
            openSwatch = openSwatch == this ? null : this;
        }

        @Override
        public boolean isMouseOver(final double mouseX, final double mouseY) {
            return this.visible && inView(mouseY) && super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void updateWidgetNarration(final NarrationElementOutput output) {
        }
    }
}
