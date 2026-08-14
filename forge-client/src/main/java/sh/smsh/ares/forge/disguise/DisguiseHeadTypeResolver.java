package sh.smsh.ares.forge.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityHorse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.disguise.DisguiseRegistry;

import java.util.Locale;

/**
 * Maps a live disguise entity to a mob-head texture key (lowercase, no {@code .png}).
 * Handles 1.8 variants that share an EntityList id (wither skeleton, horse types, etc.).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseHeadTypeResolver {

    @NotNull public static String resolveHeadKey(@Nullable final Entity entity) {
        if (entity == null) {
            return DisguiseRegistry.UNKNOWN_HEAD_KEY;
        }

        if (entity instanceof EntitySkeleton) {
            // 0 = normal, 1 = wither
            if (((EntitySkeleton) entity).getSkeletonType() == 1) {
                return "witherskeleton";
            }

            return "skeleton";
        }

        if (entity instanceof EntityHorse) {
            // 0 horse, 1 donkey, 2 mule, 3 zombie horse, 4 skeleton horse
            switch (((EntityHorse) entity).getHorseType()) {
                case 1:
                    return "donkey";
                case 3:
                    return "zombiehorse";
                case 4:
                    return "skeletonhorse";
                default:
                    // no dedicated horse/mule head assets yet
                    return DisguiseRegistry.UNKNOWN_HEAD_KEY;
            }
        }

        final String entityString = EntityList.getEntityString(entity);

        if (entityString == null || entityString.isEmpty()) {
            return DisguiseRegistry.UNKNOWN_HEAD_KEY;
        }

        // EntityList uses names like "LavaSlime", "VillagerGolem", "CaveSpider"
        return entityString.toLowerCase(Locale.ROOT);
    }
}
