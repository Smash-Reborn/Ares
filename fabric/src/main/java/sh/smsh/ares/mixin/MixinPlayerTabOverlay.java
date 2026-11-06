package sh.smsh.ares.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.fabric.disguise.DisguiseHeads;
import sh.smsh.ares.feature.impl.DisguiseDetectionFeature;
import sh.smsh.ares.gate.ServerGate;

@Mixin(PlayerTabOverlay.class)
public abstract class MixinPlayerTabOverlay {

    @Unique private PlayerInfo ares$currentInfo;
    @Unique private int ares$tabBottom = -1;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void ares$resetTabBottom(
            final GuiGraphicsExtractor graphics,
            final int screenWidth,
            final Scoreboard scoreboard,
            final Objective objective,
            final CallbackInfo ci) {
        this.ares$tabBottom = -1;
    }

    @Redirect(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/PlayerInfo;getSkin()Lnet/minecraft/world/entity/player/PlayerSkin;"
            )
    )
    private net.minecraft.world.entity.player.PlayerSkin ares$captureRow(final PlayerInfo info) {
        this.ares$currentInfo = info;
        return info.getSkin();
    }

    @Redirect(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/PlayerFaceExtractor;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIIZZI)V"
            )
    )
    private void ares$drawDisguiseHead(
            final GuiGraphicsExtractor graphics,
            final Identifier skin,
            final int x,
            final int y,
            final int size,
            final boolean hat,
            final boolean upsideDown,
            final int color) {

        final DisguiseBinding binding = ares$bindingFor(this.ares$currentInfo);
        if (binding != null) {
            graphics.blit(
                    net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                    DisguiseHeads.texture(binding.getHeadKey()),
                    x - size - 1,
                    y,
                    0.0F,
                    0.0F,
                    size,
                    size,
                    size,
                    size);
        }

        PlayerFaceExtractor.extractRenderState(graphics, skin, x, y, size, hat, upsideDown, color);
    }

    @Redirect(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"
            )
    )
    private void ares$trackPanelBottom(
            final GuiGraphicsExtractor graphics,
            final int x0,
            final int y0,
            final int x1,
            final int y1,
            final int color) {
        this.ares$tabBottom = Math.max(this.ares$tabBottom, Math.max(y0, y1));
        graphics.fill(x0, y0, x1, y1, color);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void ares$drawStatus(
            final GuiGraphicsExtractor graphics,
            final int screenWidth,
            final Scoreboard scoreboard,
            final Objective objective,
            final CallbackInfo ci) {

        if (this.ares$tabBottom < 0) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final boolean allowed = ServerGate.isAllowed();
        final Component status = Component.literal("Ares: " + (allowed ? "ENABLED" : "DISABLED"));
        final int color = allowed ? 0xFF55FF55 : 0xFFFF5555;
        graphics.centeredText(minecraft.font, status, screenWidth / 2, this.ares$tabBottom + 2, color);
    }

    @Unique
    private static DisguiseBinding ares$bindingFor(final PlayerInfo info) {
        if (!DisguiseDetectionFeature.shouldApply() || info == null) {
            return null;
        }

        final GameProfile profile = info.getProfile();
        if (profile == null || profile.name() == null || profile.name().isEmpty()) {
            return null;
        }

        return DisguiseRegistry.getByPlayerName(profile.name());
    }
}
