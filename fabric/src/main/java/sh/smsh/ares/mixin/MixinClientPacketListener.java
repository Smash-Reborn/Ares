package sh.smsh.ares.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.fabric.combat.DamageIndicators;
import sh.smsh.ares.fabric.debug.SpawnPacketDebug;
import sh.smsh.ares.fabric.disguise.DisguiseDetectionController;
import sh.smsh.ares.feature.impl.LegacyDamageTiltFeature;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    private void ares$onSpawn(final ClientboundAddEntityPacket packet, final CallbackInfo ci) {
        SpawnPacketDebug.report(packet);
        DisguiseDetectionController.onEntitySpawned(packet.getId());
    }

    @Inject(method = "handleSetEntityData", at = @At("TAIL"))
    private void ares$onEntityData(final ClientboundSetEntityDataPacket packet, final CallbackInfo ci) {
        DisguiseDetectionController.onEntityUpdated(packet.id());
        DamageIndicators.onEntityData(packet);
    }

    @Inject(method = "handleRemoveEntities", at = @At("TAIL"))
    private void ares$onRemove(final ClientboundRemoveEntitiesPacket packet, final CallbackInfo ci) {
        final int[] ids = packet.entityIds().toIntArray();
        SpawnPacketDebug.onRemoved(ids);
        DisguiseDetectionController.onEntitiesRemoved(ids);
    }

    @Inject(method = "handleSetEntityPassengersPacket", at = @At("TAIL"))
    private void ares$onPassengers(final ClientboundSetPassengersPacket packet, final CallbackInfo ci) {
        SpawnPacketDebug.onPassengers(packet);
    }

    @Redirect(
            method = "handleHurtAnimation",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;animateHurt(F)V")
    )
    private void ares$legacyDamageTilt(final Entity entity, final float yaw) {
        final float appliedYaw = LegacyDamageTiltFeature.shouldApply() ? 0.0F : yaw;
        entity.animateHurt(appliedYaw);
    }
}
