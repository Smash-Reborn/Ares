package sh.smsh.ares.labymod;

import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.AresConstants;
import sh.smsh.ares.AresLog;
import sh.smsh.ares.connection.ServerConnectionHandler;
import sh.smsh.ares.feature.FeatureManager;

import java.util.List;

public class AresLabyAddon extends LabyModAddon {

    @Override
    public void onEnable() {
        LabyFeatureBootstrap.init();

        getApi().getEventManager().registerOnJoin(serverData -> handleConnectionChange());
        getApi().getEventManager().registerOnQuit(serverData -> handleDisconnect());

        AresLog.LOGGER.info(AresConstants.MOD_NAME + " LabyMod addon enabled.");
    }

    @Override
    public void onDisable() {
        FeatureManager.clear();

        AresLog.LOGGER.info(AresConstants.MOD_NAME + " LabyMod addon disabled.");
    }

    @Override
    public void loadConfig() {
        LabyFeatureBootstrap.reloadWhitelist();
    }

    @Override
    protected void fillSettings(List<SettingsElement> subSettings) { }

    @ApiStatus.Internal
    private void handleConnectionChange() {
        ServerConnectionHandler.onConnected(resolveServerAddress());
    }

    @ApiStatus.Internal
    private void handleDisconnect() {
        ServerConnectionHandler.onDisconnected();
    }

    @ApiStatus.Internal
    @Nullable private String resolveServerAddress() {
        final net.minecraft.client.multiplayer.ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
        return serverData != null ? serverData.serverIP : null;
    }
}
