package sh.smsh.ares.mixin;

import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiPlayerTabOverlay.class)
public interface AccessorGuiPlayerTabOverlay {

    @Accessor("header")
    IChatComponent ares$getHeader();

    @Accessor("footer")
    IChatComponent ares$getFooter();

    @Invoker("drawPing")
    void ares$drawPing(int width, int x, int y, NetworkPlayerInfo info);

    @Invoker("drawScoreboardValues")
    void ares$drawScoreboardValues(
            ScoreObjective objective,
            int y,
            String name,
            int left,
            int right,
            NetworkPlayerInfo info);
}
