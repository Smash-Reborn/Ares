package sh.smsh.ares.fabric.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerType;
import sh.smsh.ares.gate.ServerGate;
import sh.smsh.ares.settings.ModSettings;

public final class AresConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 240;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 24;

    @Nullable private final Screen parent;

    public AresConfigScreen(@Nullable final Screen parent) {
        super(Component.literal("Ares Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = 36;
        final int x = this.width / 2 - BUTTON_WIDTH / 2;

        y = addToggle(x, y, "Extended Entity Render Distance", ModSettings.isExtendedRenderDistanceEnabled(), () -> {
            ModSettings.setExtendedRenderDistanceEnabled(!ModSettings.isExtendedRenderDistanceEnabled());
            return ModSettings.isExtendedRenderDistanceEnabled();
        });
        y = addAction(x, y, renderWeightLabel(), button -> {
            final int next = Math.round(ModSettings.getRenderDistanceWeight()) % 16 + 1;
            ModSettings.setRenderDistanceWeight(next);
            button.setMessage(Component.literal(renderWeightLabel()));
        });
        y = addToggle(x, y, "Disable Arrow Bouncing", ModSettings.shouldDisableArrowBouncingAnimation(), () -> {
            ModSettings.setDisableArrowBouncingAnimation(!ModSettings.shouldDisableArrowBouncingAnimation());
            return ModSettings.shouldDisableArrowBouncingAnimation();
        });
        y = addToggle(x, y, "Track Disguises", ModSettings.shouldTrackDisguises(), () -> {
            ModSettings.setTrackDisguises(!ModSettings.shouldTrackDisguises());
            return ModSettings.shouldTrackDisguises();
        });
        y = addToggle(x, y, "Render Yourself As Disguise", ModSettings.shouldShowSelfDisguiseEntity(), () -> {
            ModSettings.setShowSelfDisguiseEntity(!ModSettings.shouldShowSelfDisguiseEntity());
            return ModSettings.shouldShowSelfDisguiseEntity();
        });
        y = addToggle(x, y, "Native bounding boxes", ModSettings.shouldForceNativeBoundingBoxes(), () -> {
            ModSettings.setForceNativeBoundingBoxes(!ModSettings.shouldForceNativeBoundingBoxes());
            Minecraft.getInstance().debugEntries.rebuildCurrentList();
            return ModSettings.shouldForceNativeBoundingBoxes();
        });
        y = addToggle(x, y, "Highlight melee bounding box", ModSettings.shouldHighlightMeleeBoundingBox(), () -> {
            ModSettings.setHighlightMeleeBoundingBox(!ModSettings.shouldHighlightMeleeBoundingBox());
            return ModSettings.shouldHighlightMeleeBoundingBox();
        });
        y = addAction(x, y, highlightColorLabel(), button -> {
            if (this.minecraft != null) {
                this.minecraft.gui.setScreen(new HighlightColorScreen(this));
            }
        });
        y = addToggle(x, y, "Dynamic bounding box scale", ModSettings.shouldDynamicallyScaleDisguiseBoundingBoxes(), () -> {
            ModSettings.setDynamicallyScaleDisguiseBoundingBoxes(!ModSettings.shouldDynamicallyScaleDisguiseBoundingBoxes());
            return ModSettings.shouldDynamicallyScaleDisguiseBoundingBoxes();
        });
        y = addToggle(x, y, "Old damage tilt", ModSettings.isLegacyDamageTilt(), () -> {
            ModSettings.setLegacyDamageTilt(!ModSettings.isLegacyDamageTilt());
            return ModSettings.isLegacyDamageTilt();
        });
        y = addToggle(x, y, "Old damage sound", ModSettings.isLegacyDamageSound(), () -> {
            ModSettings.setLegacyDamageSound(!ModSettings.isLegacyDamageSound());
            return ModSettings.isLegacyDamageSound();
        });
        y = addToggle(x, y, "Disable damage indicators", ModSettings.isDisableDamageIndicators(), () -> {
            ModSettings.setDisableDamageIndicators(!ModSettings.isDisableDamageIndicators());
            return ModSettings.isDisableDamageIndicators();
        });
        y = addToggle(x, y, "Ambient disguise sounds", ModSettings.isDisableAmbientDisguiseSounds(), () -> {
            ModSettings.setDisableAmbientDisguiseSounds(!ModSettings.isDisableAmbientDisguiseSounds());
            return ModSettings.isDisableAmbientDisguiseSounds();
        });

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(x, y + 8, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(getFont(), this.title, this.width / 2, 12, 0xFFFFFFFF);

        final ServerType serverType = ServerGate.getCurrentServerType();
        final boolean known = serverType != ServerType.UNKNOWN;
        graphics.centeredText(
                getFont(),
                Component.literal("ServerGate: " + (known ? serverType.getDisplayName() : serverType.name())),
                this.width / 2,
                24,
                known ? 0xFFFFAA00 : 0xFFFF5555);
    }

    @Override
    public void onClose() {
        ModSettings.saveConfig();
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    private int addToggle(final int x, final int y, final String label, final boolean enabled, final ToggleAction action) {
        addRenderableWidget(Button.builder(Component.literal(toggleLabel(label, enabled)), button -> {
            final boolean now = action.toggle();
            button.setMessage(Component.literal(toggleLabel(label, now)));
        }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        return y + SPACING;
    }

    private int addAction(final int x, final int y, final String label, final Button.OnPress onPress) {
        addRenderableWidget(Button.builder(Component.literal(label), onPress)
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        return y + SPACING;
    }

    private static String toggleLabel(final String label, final boolean enabled) {
        return label + ": " + (enabled ? "ON" : "OFF");
    }

    private static String renderWeightLabel() {
        return "Entity Render Weight: " + Math.round(ModSettings.getRenderDistanceWeight());
    }

    private static String highlightColorLabel() {
        return "Highlight color: " + ModSettings.getHighlightMeleeBoundingBoxColor().name();
    }

    @FunctionalInterface
    private interface ToggleAction {
        boolean toggle();
    }
}
