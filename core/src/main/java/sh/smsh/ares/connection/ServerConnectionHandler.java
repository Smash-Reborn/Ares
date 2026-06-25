package sh.smsh.ares.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.AresBootstrap;
import sh.smsh.ares.AresConstants;
import sh.smsh.ares.AresLog;
import sh.smsh.ares.feature.FeatureManager;
import sh.smsh.ares.gate.ServerGate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerConnectionHandler {

    public static void onConnected(@Nullable final String address) {
        AresBootstrap.reloadConfig();
        applyConnectionState(address);
    }

    public static void onServerAddressChanged(@Nullable final String address) {
        applyConnectionState(address);
    }

    public static void onDisconnected() {
        ServerGate.clearCurrentServer();
        FeatureManager.disableAll();
        AresLog.LOGGER.info(AresConstants.MOD_NAME + " inactive - disconnected from server.");
    }

    private static void applyConnectionState(@Nullable final String address) {
        ServerGate.updateCurrentServer(address);
        FeatureManager.evaluateGate();
        logConnectionState(address);
    }

    private static void logConnectionState(@Nullable final String address) {
        if (address == null) {
            AresLog.LOGGER.info(AresConstants.MOD_NAME + " inactive - not connected to a multiplayer server.");
            return;
        }

        if (ServerGate.isAllowed()) {
            AresLog.LOGGER.info(AresConstants.MOD_NAME + " active on {}", address);
        } else {
            AresLog.LOGGER.info(AresConstants.MOD_NAME + " inactive - server not whitelisted ({})", address);
        }
    }
}