package sh.smsh.ares.fabric.disguise;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.config.AresColor;
import sh.smsh.ares.config.ModSettingsConfig;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.disguise.SidebarStocks;
import sh.smsh.ares.feature.impl.DisguiseGlowFeature;
import sh.smsh.ares.settings.ModSettings;
import sh.smsh.ares.mixin.MixinEntityFlags;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseGlowController {

    private static final String TRACKING_COMPASS_NAME = "TRACKING COMPASS";

    @NotNull private static Map<Integer, Integer> glowByEntity = Map.of();
    private static boolean glowLatched;
    private static boolean keyWasDown;
    private static boolean useWasDown;

    public static void refresh() {
        if (!DisguiseGlowFeature.shouldApply()) {
            glowByEntity = Map.of();
            keyWasDown = false;
            useWasDown = false;
            return;
        }

        pollToggle();
        if (!glowActive()) {
            glowByEntity = Map.of();
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            glowByEntity = Map.of();
            return;
        }

        final Map<String, Integer> stocks = SidebarStocks.parse(SidebarLines.current());
        final Map<Integer, Integer> next = new HashMap<>();
        for (final DisguiseBinding binding : DisguiseRegistry.getAll()) {
            if (binding.isSelfInferred()) {
                continue;
            }

            final Entity entity = minecraft.level.getEntity(binding.getEntityId());
            if (!isVisibleDisguise(entity)) {
                continue;
            }

            final AresColor color = ModSettings.getDisguiseGlowColor(
                    SidebarStocks.get(stocks, binding.getPlayerName()));
            next.put(binding.getEntityId(), color.getRgb());
        }
        glowByEntity = next;
    }

    public static void clear() {
        glowByEntity = Map.of();
        glowLatched = false;
        keyWasDown = false;
        useWasDown = false;
        DisguiseGlowOcclusion.clearMarks();
    }

    /** Mod disguise glow that should be occluded. Server glow keeps the vanilla outline. */
    public static boolean occludesGlow(@NotNull final Entity entity) {
        return glowRgb(entity) != null && !((MixinEntityFlags) entity).ares$getSharedFlag(6);
    }

    /**
     * RGB to force onto this disguise, or null when the mod should leave vanilla glow alone.
     * Callers must skip this while the server glowing flag is set.
     */
    @Nullable
    public static Integer glowRgb(@NotNull final Entity entity) {
        return glowByEntity.get(entity.getId());
    }

    private static void pollToggle() {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        final ModSettingsConfig.DisguiseGlowSettings.DisguiseGlowToggleType type = ModSettings.getDisguiseGlowToggleType();
        if (player == null) {
            keyWasDown = false;
            useWasDown = false;
            return;
        }
        if (minecraft.gui.screen() != null) {
            return;
        }

        if (type == ModSettingsConfig.DisguiseGlowSettings.DisguiseGlowToggleType.TOGGLE_VIA_BUTTON_BINDED_PRESS) {
            final int key = ModSettings.getDisguiseGlowToggleKeyCode();
            final boolean down = key > 0 && InputConstants.isKeyDown(key);
            if (down && !keyWasDown) {
                glowLatched = !glowLatched;
            }
            keyWasDown = down;
        } else {
            keyWasDown = false;
        }

        if (type == ModSettingsConfig.DisguiseGlowSettings.DisguiseGlowToggleType.TOGGLE_VIA_COMPASS_CLICK) {
            final boolean down = minecraft.options.keyUse.isDown() && holdingTrackingCompass(player);
            if (down && !useWasDown) {
                glowLatched = !glowLatched;
            }
            useWasDown = down;
        } else {
            useWasDown = false;
        }
    }

    private static boolean glowActive() {
        final LocalPlayer player = Minecraft.getInstance().player;
        return switch (ModSettings.getDisguiseGlowToggleType()) {
            case PERMANENTLY_ACTIVE -> true;
            case ACTIVE_ONLY_WHEN_HOLDING_COMPASS -> player != null && holdingTrackingCompass(player);
            case TOGGLE_VIA_COMPASS_CLICK, TOGGLE_VIA_BUTTON_BINDED_PRESS -> glowLatched;
        };
    }

    private static boolean holdingTrackingCompass(@NotNull final LocalPlayer player) {
        return isTrackingCompass(player.getItemInHand(InteractionHand.MAIN_HAND))
                || isTrackingCompass(player.getItemInHand(InteractionHand.OFF_HAND));
    }

    private static boolean isTrackingCompass(@NotNull final ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.COMPASS) {
            return false;
        }
        return TRACKING_COMPASS_NAME.equalsIgnoreCase(TextFormattingUtil.stripFormatting(stack.getHoverName().getString()));
    }

    private static boolean isVisibleDisguise(@Nullable final Entity entity) {
        return entity != null && entity.isAlive() && !entity.isRemoved() && !entity.isInvisible() && !entity.isSpectator();
    }
}
