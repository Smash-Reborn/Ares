package sh.smsh.ares.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.AresConstants;
import sh.smsh.ares.AresLog;

@Mod(
    modid = AresConstants.MOD_ID,
    name = AresConstants.MOD_NAME,
    version = AresConstants.VERSION,
    clientSideOnly = true,
    acceptableRemoteVersions = "*"
)
public class AresMod {

    @Mod.Instance(AresConstants.MOD_ID)
    public static AresMod instance;

    @Mod.EventHandler
    public void preInit(@NotNull final FMLPreInitializationEvent event) {
        ForgeFeatureBootstrap.init(event.getModConfigurationDirectory().getParentFile());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ForgeServerProbe());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ForgeClientTickHandler());
    }

    @Mod.EventHandler
    public void init(@NotNull final FMLInitializationEvent event) {
        AresLog.LOGGER.info(AresConstants.MOD_NAME + " loaded.");
    }
}