package sh.smsh.ares.forge.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.disguise.DisguiseRegistry;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseHeadTextures {

    private static final String DOMAIN = "aresmod";
    private static final String PATH_PREFIX = "textures/gui/mob_heads/";
    private static final ResourceLocation UNKNOWN =
            new ResourceLocation(DOMAIN, PATH_PREFIX + DisguiseRegistry.UNKNOWN_HEAD_KEY + ".png");

    @NotNull private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    @NotNull
    public static ResourceLocation get(@Nullable final String headKey) {
        if (headKey == null || headKey.isEmpty()) {
            return UNKNOWN;
        }

        final String key = headKey.toLowerCase(Locale.ROOT);
        final ResourceLocation cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        final ResourceLocation location = new ResourceLocation(DOMAIN, PATH_PREFIX + key + ".png");
        if (!resourceExists(location)) {
            CACHE.put(key, UNKNOWN);
            return UNKNOWN;
        }

        CACHE.put(key, location);
        return location;
    }

    public static void drawHead(@NotNull final String headKey, final int x, final int y) {
        final ResourceLocation texture = get(headKey);
        final Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // Full 8x8 (or larger) PNG scaled into the tab head slot
        Gui.drawScaledCustomSizeModalRect(x, y, 0.0F, 0.0F, 8, 8, 8, 8, 8.0F, 8.0F);
    }

    private static boolean resourceExists(@NotNull final ResourceLocation location) {
        try {
            return Minecraft.getMinecraft().getResourceManager().getResource(location) != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
