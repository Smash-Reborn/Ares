package sh.smsh.ares.fabric.command;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.fabric.debug.SpawnPacketDebug;
import sh.smsh.ares.fabric.gui.AresGuiScheduler;

public final class AresClientCommands {

    private AresClientCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("ssmprefs")
                    .executes(context -> openSettings()));
            dispatcher.register(ClientCommands.literal("aresconfig")
                    .executes(context -> openSettings()));
            dispatcher.register(ClientCommands.literal("aressettings")
                    .executes(context -> openSettings()));
            dispatcher.register(ClientCommands.literal("ssmconfig")
                    .executes(context -> openSettings()));
            dispatcher.register(ClientCommands.literal("ssmsettings")
                    .executes(context -> openSettings()));

            dispatcher.register(ClientCommands.literal("aresdebug")
                    .then(ClientCommands.literal("disguises")
                            .executes(context -> {
                                final var source = context.getSource();
                                final var bindings = DisguiseRegistry.getAll();
                                source.sendFeedback(Component.literal("[Ares] Tracked disguises: " + bindings.size()));
                                for (final DisguiseBinding binding : bindings) {
                                    source.sendFeedback(Component.literal("- " + binding.getPlayerName()
                                            + " -> " + binding.getHeadKey()
                                            + " (id " + binding.getEntityId()
                                            + (binding.isSelfInferred() ? ", self" : "")
                                            + ")"));
                                }
                                return 1;
                            })));

            dispatcher.register(ClientCommands.literal("commanddebugpackets")
                    .executes(context -> {
                        final boolean enabled = SpawnPacketDebug.toggle();
                        context.getSource().sendFeedback(Component.literal(
                                "[Ares] Spawn packet debug " + (enabled ? "enabled" : "disabled")));
                        return 1;
                    }));
        });
    }

    private static int openSettings() {
        AresGuiScheduler.requestConfigScreen();
        return 1;
    }
}
