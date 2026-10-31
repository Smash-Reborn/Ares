package sh.smsh.ares.forge.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.forge.gui.AresGuiScheduler;

import java.util.Arrays;
import java.util.List;

public class AresConfigCommand extends CommandBase {

    @Override
    @NotNull public String getCommandName() {
        return "aresconfig";
    }

    @Override
    @NotNull public String getCommandUsage(@NotNull final ICommandSender sender) {
        return "/aresconfig";
    }

    @Override
    @NotNull public List<String> getCommandAliases() {
        return Arrays.asList("aressettings", "ssmconfig", "ssmsettings");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(@NotNull final ICommandSender sender, @NotNull final String[] args) {
        AresGuiScheduler.requestConfigScreen();
        // see AresGuiScheduler for why we delay opening up the settings gui
    }
}
