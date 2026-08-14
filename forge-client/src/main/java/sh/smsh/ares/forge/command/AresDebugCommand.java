package sh.smsh.ares.forge.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AresDebugCommand extends CommandBase {

    @Override
    @NotNull public String getCommandName() {
        return "aresdebug";
    }

    @Override
    @NotNull public String getCommandUsage(@NotNull final ICommandSender sender) {
        return "/aresdebug <disguises>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    @NotNull public List<String> addTabCompletionOptions(
            @NotNull final ICommandSender sender,
            @NotNull final String[] args,
            final net.minecraft.util.BlockPos pos) {

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "disguises");
        }

        return Collections.emptyList();
    }

    @Override
    public void processCommand(@NotNull final ICommandSender sender, @NotNull final String[] args) {
        if (args.length < 1) {
            send(sender, EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender));
            return;
        }

        if ("disguises".equalsIgnoreCase(args[0])) {
            printDisguises(sender);
            return;
        }

        send(sender, EnumChatFormatting.RED + "Unknown argument. Usage: " + getCommandUsage(sender));
    }

    private void printDisguises(@NotNull final ICommandSender sender) {
        final Collection<DisguiseBinding> bindings = DisguiseRegistry.getAll();
        send(sender, EnumChatFormatting.GOLD + "[Ares] Tracked disguises: "
                + EnumChatFormatting.WHITE + bindings.size());

        if (bindings.isEmpty()) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        for (final DisguiseBinding binding : bindings) {
            final String entityType = resolveEntityType(mc, binding.getEntityId());
            send(sender, EnumChatFormatting.YELLOW + "- "
                    + EnumChatFormatting.AQUA + binding.getPlayerName()
                    + EnumChatFormatting.GRAY + " -> "
                    + EnumChatFormatting.WHITE + entityType
                    + EnumChatFormatting.DARK_GRAY + " / head=" + binding.getHeadKey()
                    + " (id " + binding.getEntityId() + ")");
        }
    }

    @NotNull private String resolveEntityType(@NotNull final Minecraft mc, final int entityId) {
        if (mc.theWorld == null) {
            return "unknown";
        }

        final Entity entity = mc.theWorld.getEntityByID(entityId);
        if (entity == null) {
            return "unloaded";
        }

        final String entityString = EntityList.getEntityString(entity);
        if (entityString != null && !entityString.isEmpty()) {
            return entityString;
        }

        return entity.getClass().getSimpleName();
    }

    private void send(@NotNull final ICommandSender sender, @NotNull final String message) {
        sender.addChatMessage(new ChatComponentText(message));
    }
}
