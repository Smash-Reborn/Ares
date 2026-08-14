package sh.smsh.ares.forge;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerConnectionHandler;
import sh.smsh.ares.forge.disguise.DisguiseDetectionController;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ForgeServerProbe {

    private String lastObservedAddress;

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        final String address = resolveServerAddress();
        ServerConnectionHandler.onConnected(address);
        lastObservedAddress = address;
    }

    @SubscribeEvent
    public void onClientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        DisguiseDetectionController.clear();
        ServerConnectionHandler.onDisconnected();
        lastObservedAddress = null;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.world.isRemote) {
            return;
        }

        DisguiseDetectionController.clear();

        final String address = resolveServerAddress();
        if (address != null && address.equals(lastObservedAddress)) {
            return;
        }

        ServerConnectionHandler.onServerAddressChanged(address);
        lastObservedAddress = address;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!event.world.isRemote) {
            return;
        }

        DisguiseDetectionController.clear();
    }

    @ApiStatus.Internal
    @Nullable private String resolveServerAddress() {
        final ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
        return serverData != null ? serverData.serverIP : null;
    }
}
