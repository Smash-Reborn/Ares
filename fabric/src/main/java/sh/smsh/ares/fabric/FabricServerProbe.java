package sh.smsh.ares.fabric;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.ServerData;
import sh.smsh.ares.connection.ServerConnectionHandler;
import sh.smsh.ares.fabric.disguise.DisguiseDetectionController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FabricServerProbe {

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            final ServerData serverData = client.getCurrentServer();
            ServerConnectionHandler.onConnected(serverData != null ? serverData.ip : null);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ServerConnectionHandler.onDisconnected();
            DisguiseDetectionController.clear();
        });
    }
}
