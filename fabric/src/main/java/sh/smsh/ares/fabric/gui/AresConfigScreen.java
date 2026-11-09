package sh.smsh.ares.fabric.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
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
    @Nullable private StatusButton glowKeyButton;
    private boolean listeningForGlowKey;
    private int scrollOffset;
    private boolean draggingScroll;
    @Nullable private String statusMessage;
    @Nullable private PreviewKind previewKind;
    private long previewUntil;

    public AresConfigScreen(@Nullable final Screen parent) {
        super(Component.literal("Ares Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.openSwatch = null;
        this.listeningForGlowKey = false;
        this.glowKeyButton = null;
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
        addToggle("Self Disguise on Crack",
                "When true, disguises display using a broken flailing render bug, causing their arms, legs and other appendages to constantly move like the disguise is on crack. This only applies to self-disguises. Set to false to make the disguise render and move like normal",
                ModSettings.isSelfDisguiseOnCrack(), () -> {
            ModSettings.setSelfDisguiseOnCrack(!ModSettings.isSelfDisguiseOnCrack());
            return ModSettings.isSelfDisguiseOnCrack();
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
        addColorSwatch(
                "Highlight color",
                null,
                ModSettings.getHighlightMeleeBoundingBoxColor(),
                ModSettings::setHighlightMeleeBoundingBoxColor);
        addToggle("Dynamic Melee Bounding Box",
                "Tools now provide a specialized item component which grows the hitbox_margin of a targets bounding box by 0.1, mimicking 1.8s clientside melee bounding box growth. If enabled, this feature makes the \"highlighting\" of the bounding box respect the new margin.",
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
        addToggle("Disable Damage Indicators",
                "When enabled, you will not render any text display entities which are used as damage indicators.",
                ModSettings.isDisableDamageIndicators(), () -> {
            ModSettings.setDisableDamageIndicators(!ModSettings.isDisableDamageIndicators());
            return ModSettings.isDisableDamageIndicators();
        });
        addToggle("Disable Ambient Disguise Sounds",
                "When enabled, a sound is cancelled if it belongs to a mob type that any active disguise is using.",
                ModSettings.isDisableAmbientDisguiseSounds(), () -> {
            ModSettings.setDisableAmbientDisguiseSounds(!ModSettings.isDisableAmbientDisguiseSounds());
            return ModSettings.isDisableAmbientDisguiseSounds();
        });
        addToggle("Fire Overlay",
                "When enabled, the first-person fire overlay uses the height and opacity sliders below. 0.00 height is the vanilla position, and 100% opacity is unchanged vanilla fire.",
                ModSettings.isFireOverlayEnabled(), () -> {
            ModSettings.setFireOverlayEnabled(!ModSettings.isFireOverlayEnabled());
            return ModSettings.isFireOverlayEnabled();
        });
        addUnitSlider(
                "Fire Height Offset",
                "Lowers the fire overlay. 0.00 keeps it at the vanilla position. Higher values push the flames further down the screen.",
                ModSettings.getFireOverlayHeightOffset(),
                ModSettings.MIN_FIRE_OVERLAY_OFFSET,
                ModSettings.MAX_FIRE_OVERLAY_OFFSET,
                SliderText.OFFSET,
                ModSettings::setFireOverlayHeightOffset,
                PreviewKind.FIRE);
        addUnitSlider(
                "Fire Opacity",
                "Opacity of the fire overlay. 100% is vanilla. Lower values make the flames more see-through, down to 1%.",
                ModSettings.getFireOverlayOpacity(),
                ModSettings.MIN_VISUAL_OPACITY,
                ModSettings.MAX_VISUAL_OPACITY,
                SliderText.PERCENT,
                ModSettings::setFireOverlayOpacity,
                PreviewKind.FIRE);
        addToggle("Particle Opacity",
                "When enabled, rendered particles use the opacity slider below. 100% is unchanged vanilla.",
                ModSettings.isParticleOpacityEnabled(), () -> {
            ModSettings.setParticleOpacityEnabled(!ModSettings.isParticleOpacityEnabled());
            return ModSettings.isParticleOpacityEnabled();
        });
        addUnitSlider(
                "Particle Opacity Level",
                "Opacity applied to particles. 100% is vanilla. Lower values make them more see-through, down to 1%.",
                ModSettings.getParticleOpacity(),
                ModSettings.MIN_VISUAL_OPACITY,
                ModSettings.MAX_VISUAL_OPACITY,
                SliderText.PERCENT,
                ModSettings::setParticleOpacity,
                PreviewKind.PARTICLES);
        addToggle("Disguise Glow",
                "When enabled, valid visible disguises glow. With stock colors off, every disguise uses the fallback color. A glow sent by the server, such as a hit that forces a disguise white, replaces the mod glow until the server glow ends.",
                ModSettings.isDisguiseGlowEnabled(), () -> {
            ModSettings.setDisguiseGlowEnabled(!ModSettings.isDisguiseGlowEnabled());
            return ModSettings.isDisguiseGlowEnabled();
        });
        addGlowMode();
        addGlowKey();
        addToggle("Stock Colors",
                "When enabled, the sidebar stock count picks the glow color. 4 or more uses the 4-stock color, and 3, 2, and 1 use their own colors. A missing or invalid count uses the fallback color. A leading team square on teams-mode lines is ignored.",
                ModSettings.useCustomDisguiseStockColors(), () -> {
            ModSettings.setUseCustomDisguiseStockColors(!ModSettings.useCustomDisguiseStockColors());
            return ModSettings.useCustomDisguiseStockColors();
        });
        addColorSwatch(
                "4+ Stock Color",
                "Glow color for a disguise at 4 stocks, and for any custom count above 4.",
                ModSettings.getDisguiseGlowFourStocksColor(),
                ModSettings::setDisguiseGlowFourStocksColor);
        addColorSwatch(
                "3 Stock Color",
                "Glow color for a disguise at 3 stocks.",
                ModSettings.getDisguiseGlowThreeStocksColor(),
                ModSettings::setDisguiseGlowThreeStocksColor);
        addColorSwatch(
                "2 Stock Color",
                "Glow color for a disguise at 2 stocks.",
                ModSettings.getDisguiseGlowTwoStocksColor(),
                ModSettings::setDisguiseGlowTwoStocksColor);
        addColorSwatch(
                "1 Stock Color",
                "Glow color for a disguise at 1 stock.",
                ModSettings.getDisguiseGlowOneStockColor(),
                ModSettings::setDisguiseGlowOneStockColor);
        addColorSwatch(
                "Fallback Glow Color",
                "Glow color when stock colors are off, and when a disguise has no valid stock count.",
                ModSettings.getDisguiseGlowFallbackColor(),
                ModSettings::setDisguiseGlowFallbackColor);

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

        if (previewActive(PreviewKind.FIRE)) {
            drawFirePreview(graphics);
        } else if (previewActive(PreviewKind.PARTICLES)) {
            drawParticlePreview(graphics);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubled) {
        if (this.openSwatch != null) {
            final AresColor picked = colorAt(event.x(), event.y());
            if (picked != null) {
                this.openSwatch.setColor(picked);
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
    public boolean keyPressed(final KeyEvent event) {
        if (this.listeningForGlowKey) {
            captureGlowKey(event);
            return true;
        }
        return super.keyPressed(event);
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

    private void addUnitSlider(
            final String label,
            @Nullable final String description,
            final float value,
            final float min,
            final float max,
            final SliderText text,
            final FloatConsumer apply,
            final PreviewKind preview) {
        final int index = this.rows.size();
        final UnitSlider slider = new UnitSlider(controlX(), 0, value, min, max, text, apply, preview);
        addRenderableWidget(slider);
        this.rows.add(new SettingRow(label, description, slider, 0, index));
    }

    private void showPreview(final PreviewKind kind, final boolean hold) {
        if (!hold && this.previewKind == kind && this.previewUntil == Long.MAX_VALUE) {
            return;
        }
        this.previewKind = kind;
        this.previewUntil = hold ? Long.MAX_VALUE : System.currentTimeMillis() + 250L;
    }

    private void clearHeldPreview(final PreviewKind kind) {
        if (this.previewKind == kind && this.previewUntil == Long.MAX_VALUE) {
            this.previewKind = null;
            this.previewUntil = 0L;
        }
    }

    private boolean previewActive(final PreviewKind kind) {
        return this.previewKind == kind
                && (this.previewUntil == Long.MAX_VALUE || System.currentTimeMillis() < this.previewUntil);
    }

    private void drawFirePreview(final GuiGraphicsExtractor graphics) {
        final TextureAtlasSprite primary = graphics.getSprite(ModelBakery.FIRE_1);
        final TextureAtlasSprite secondary = graphics.getSprite(ModelBakery.FIRE_0);
        if (primary == null || secondary == null) {
            return;
        }

        final float span = Math.max(0.0001f, ModSettings.MAX_FIRE_OVERLAY_OFFSET - ModSettings.MIN_FIRE_OVERLAY_OFFSET);
        final float lowered = (ModSettings.getFireOverlayHeightOffset() - ModSettings.MIN_FIRE_OVERLAY_OFFSET) / span;
        final int alpha = Math.max(1, Math.round(0xE5 * ModSettings.getFireOverlayOpacity()));
        final int color = ARGB.color(alpha, 255, 255, 255);
        final int band = Math.max(64, this.height / 3);
        final int y = this.height - band + Math.round(lowered * (band + 24));
        final boolean swap = (System.currentTimeMillis() / 100L & 1L) == 0L;
        for (int x = 0, tileIndex = 0; x < this.width; x += band, tileIndex++) {
            final TextureAtlasSprite sprite = (tileIndex % 2 == 0) == swap ? primary : secondary;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, band, band, color);
        }
    }

    private void drawParticlePreview(final GuiGraphicsExtractor graphics) {
        final int alpha = Math.max(1, Math.round(255f * ModSettings.getParticleOpacity()));
        final int color = ARGB.color(alpha, 186, 92, 255);
        final int centerX = this.width / 2;
        final int centerY = this.height / 2;
        final long time = System.currentTimeMillis();
        final int count = 16;
        for (int i = 0; i < count; i++) {
            final int frame = (int) ((time / 70L + i * 3L) % 8L);
            final TextureAtlasSprite sprite = graphics.getSprite(new SpriteId(
                    Identifier.withDefaultNamespace("textures/atlas/particles.png"),
                    Identifier.withDefaultNamespace("effect_" + frame)));
            if (sprite == null) {
                continue;
            }

            final double spin = time / 450.0 + i * (Math.PI * 2.0 / count);
            final double rise = (time / 16.0 + i * 22.0) % 110.0;
            final double radius = 26.0 + (i % 5) * 14.0;
            final int size = 8 + (i % 4) * 5;
            final int x = centerX + (int) (Math.cos(spin) * radius) - size / 2;
            final int y = centerY + 36 - (int) rise + (int) (Math.sin(spin) * 18.0) - size / 2;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, size, size, color);
        }
    }

    private void addGlowMode() {
        final int index = this.rows.size();
        final StatusButton button = new StatusButton(controlX(), 0, glowModeMessage(), pressed -> {
            ModSettings.cycleDisguiseGlowToggleType();
            pressed.setMessage(glowModeMessage());
        });
        addRenderableWidget(button);
        this.rows.add(new SettingRow(
                "Glow Mode",
                "Permanently Active always glows valid disguises. Holding Compass glows only while you hold a compass named TRACKING COMPASS. Compass Click and Keybind toggle that glow on and off.",
                button,
                0,
                index));
    }

    private void addGlowKey() {
        final int index = this.rows.size();
        this.glowKeyButton = new StatusButton(controlX(), 0, glowKeyMessage(), pressed -> {
            this.listeningForGlowKey = !this.listeningForGlowKey;
            pressed.setMessage(glowKeyMessage());
        });
        addRenderableWidget(this.glowKeyButton);
        this.rows.add(new SettingRow(
                "Glow Toggle Key",
                "Click, then press a keyboard key to bind the keybind glow toggle. Press Escape to cancel. Click the button again to stop listening.",
                this.glowKeyButton,
                0,
                index));
    }

    private void captureGlowKey(final KeyEvent event) {
        final InputConstants.Key key = InputConstants.getKey(event);
        if (key.getType() != InputConstants.Type.KEYBOARD) {
            return;
        }
        if (key.getValue() != InputConstants.KEY_ESCAPE) {
            ModSettings.setDisguiseGlowToggleKeyCode(key.getValue());
        }
        this.listeningForGlowKey = false;
        if (this.glowKeyButton != null) {
            this.glowKeyButton.setMessage(glowKeyMessage());
        }
    }

    private Component glowModeMessage() {
        return Component.literal(ModSettings.getDisguiseGlowToggleType().getLabel()).withStyle(ChatFormatting.GOLD);
    }

    private Component glowKeyMessage() {
        if (this.listeningForGlowKey) {
            return Component.literal("Press a key...").withStyle(ChatFormatting.GOLD);
        }
        final int code = ModSettings.getDisguiseGlowToggleKeyCode();
        if (code <= 0) {
            return Component.literal("Unbound").withStyle(ChatFormatting.RED);
        }
        return InputConstants.Type.KEYBOARD.getOrCreate(code).getDisplayName().copy().withStyle(ChatFormatting.GREEN);
    }

    private void addColorSwatch(
            final String label,
            @Nullable final String description,
            final AresColor color,
            final ColorConsumer apply) {
        final int index = this.rows.size();
        final ColorSwatch swatch = new ColorSwatch(controlX() + CONTROL_WIDTH - SWATCH_SIZE, 0, color, apply);
        addRenderableWidget(swatch);
        this.rows.add(new SettingRow(label, description, swatch, 1, index));
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

    @FunctionalInterface
    private interface FloatConsumer {
        void accept(float value);
    }

    @FunctionalInterface
    private interface ColorConsumer {
        void accept(AresColor color);
    }

    private enum PreviewKind {
        FIRE,
        PARTICLES
    }

    private enum SliderText {
        PERCENT,
        OFFSET
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

    private final class UnitSlider extends AbstractSliderButton {

        private final float min;
        private final float max;
        private final SliderText text;
        private final FloatConsumer apply;
        private final PreviewKind preview;

        private UnitSlider(
                final int x,
                final int y,
                final float value,
                final float min,
                final float max,
                final SliderText text,
                final FloatConsumer apply,
                final PreviewKind preview) {
            super(x, y, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty(), (value - min) / (double) (max - min));
            this.min = min;
            this.max = max;
            this.text = text;
            this.apply = apply;
            this.preview = preview;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            final float current = currentValue();
            final String label = this.text == SliderText.PERCENT
                    ? Math.round(current * 100f) + "%"
                    : String.format(Locale.ROOT, "%.2f", current);
            setMessage(Component.literal(label));
        }

        @Override
        protected void applyValue() {
            this.apply.accept(currentValue());
            showPreview(this.preview, false);
        }

        @Override
        public void onClick(final MouseButtonEvent event, final boolean doubled) {
            super.onClick(event, doubled);
            showPreview(this.preview, true);
        }

        @Override
        public void onRelease(final MouseButtonEvent event) {
            super.onRelease(event);
            clearHeldPreview(this.preview);
        }

        @Override
        public boolean keyPressed(final KeyEvent event) {
            final boolean handled = super.keyPressed(event);
            if (handled && (event.isLeft() || event.isRight())) {
                showPreview(this.preview, false);
            }
            return handled;
        }

        @Override
        public boolean isMouseOver(final double mouseX, final double mouseY) {
            return this.visible && inView(mouseY) && super.isMouseOver(mouseX, mouseY);
        }

        private float currentValue() {
            final float raw = this.min + (float) this.value * (this.max - this.min);
            final float snapped = Math.round(raw * 100f) / 100f;
            return Math.max(this.min, Math.min(this.max, snapped));
        }
    }

    private final class ColorSwatch extends AbstractWidget {

        private AresColor color;
        private final ColorConsumer apply;

        private ColorSwatch(final int x, final int y, final AresColor color, final ColorConsumer apply) {
            super(x, y, SWATCH_SIZE, SWATCH_SIZE, Component.empty());
            this.color = color;
            this.apply = apply;
        }

        private void setColor(final AresColor color) {
            this.color = color;
            this.apply.accept(color);
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
