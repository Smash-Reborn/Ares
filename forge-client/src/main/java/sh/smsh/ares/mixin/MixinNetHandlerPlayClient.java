package sh.smsh.ares.mixin;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.smsh.ares.forge.disguise.DisguiseDetectionController;
import sh.smsh.ares.forge.disguise.MineplexNametagFixController;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Inject(method = "handleSpawnMob", at = @At("RETURN"))
    private void ares$onSpawnMob(final S0FPacketSpawnMob packet, final CallbackInfo ci) {
        if (packet == null) {
            return;
        }

        DisguiseDetectionController.onLivingEntitySpawnOrMetadata(packet.getEntityID());
        MineplexNametagFixController.onMobSpawn(packet.getEntityID());
    }

    @Inject(method = "handleSpawnObject", at = @At("RETURN"))
    private void ares$onSpawnObject(final S0EPacketSpawnObject packet, final CallbackInfo ci) {
        if (packet != null) {
            MineplexNametagFixController.onObjectSpawn(packet.getEntityID(), packet.getType());
        }
    }

    @Inject(method = "handleEntityMetadata", at = @At("RETURN"))
    private void ares$onEntityMetadata(final S1CPacketEntityMetadata packet, final CallbackInfo ci) {
        if (packet == null) {
            return;
        }

        DisguiseDetectionController.onLivingEntitySpawnOrMetadata(packet.getEntityId());
        MineplexNametagFixController.onEntityMetadata(packet.getEntityId());
    }

    @Inject(method = "handleEntityAttach", at = @At("RETURN"))
    private void ares$onEntityAttach(final S1BPacketEntityAttach packet, final CallbackInfo ci) {
        if (packet != null) {
            MineplexNametagFixController.onEntityAttach(packet.getEntityId(), packet.getVehicleEntityId());
        }
    }

    @Inject(method = "handleDestroyEntities", at = @At("RETURN"))
    private void ares$onDestroyEntities(final S13PacketDestroyEntities packet, final CallbackInfo ci) {
        if (packet != null && packet.getEntityIDs() != null) {
            DisguiseDetectionController.onEntitiesDestroyed(packet.getEntityIDs());
        }
    }
}
