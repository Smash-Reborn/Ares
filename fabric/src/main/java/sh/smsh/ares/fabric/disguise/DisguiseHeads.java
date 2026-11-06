package sh.smsh.ares.fabric.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.disguise.DisguiseRegistry;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseHeads {

    private static final String NAMESPACE = "aresmod";

    private static final Map<EntityType<?>, String> HEAD_KEYS = Map.ofEntries(
            Map.entry(EntityTypes.BLAZE, "blaze"),
            Map.entry(EntityTypes.CAVE_SPIDER, "cavespider"),
            Map.entry(EntityTypes.CHICKEN, "chicken"),
            Map.entry(EntityTypes.COW, "cow"),
            Map.entry(EntityTypes.CREEPER, "creeper"),
            Map.entry(EntityTypes.DONKEY, "donkey"),
            Map.entry(EntityTypes.ENDERMAN, "enderman"),
            Map.entry(EntityTypes.GUARDIAN, "guardian"),
            Map.entry(EntityTypes.MAGMA_CUBE, "lavaslime"),
            Map.entry(EntityTypes.PIG, "pig"),
            Map.entry(EntityTypes.SHEEP, "sheep"),
            Map.entry(EntityTypes.SKELETON, "skeleton"),
            Map.entry(EntityTypes.SKELETON_HORSE, "skeletonhorse"),
            Map.entry(EntityTypes.SLIME, "slime"),
            Map.entry(EntityTypes.SNOW_GOLEM, "snowman"),
            Map.entry(EntityTypes.SPIDER, "spider"),
            Map.entry(EntityTypes.SQUID, "squid"),
            Map.entry(EntityTypes.VILLAGER, "villager"),
            Map.entry(EntityTypes.IRON_GOLEM, "villagergolem"),
            Map.entry(EntityTypes.WITCH, "witch"),
            Map.entry(EntityTypes.WITHER_SKELETON, "witherskeleton"),
            Map.entry(EntityTypes.WOLF, "wolf"),
            Map.entry(EntityTypes.ZOMBIE, "zombie"),
            Map.entry(EntityTypes.ZOMBIE_HORSE, "zombiehorse"),
            Map.entry(EntityTypes.GOAT, "goat"),
            Map.entry(EntityTypes.MOOSHROOM, "mooshroom"),
            Map.entry(EntityTypes.PHANTOM, "phantom"),

            // COMING SOON
            Map.entry(EntityTypes.POLAR_BEAR, "polarbear"),
            Map.entry(EntityTypes.BREEZE, "breeze"),
            Map.entry(EntityTypes.EVOKER, "evoker"),
            Map.entry(EntityTypes.HOGLIN, "hoglin"),
            Map.entry(EntityTypes.DROWNED, "drowned"),
            Map.entry(EntityTypes.LLAMA, "llama"),
            Map.entry(EntityTypes.PILLAGER, "evoker")       // they p much have the exact same face lol
    );

    private static final Map<String, EntityType<?>> ENTITY_TYPES = HEAD_KEYS.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (first, ignored) -> first));

    @NotNull
    public static String resolveHeadKey(@Nullable final Entity entity) {
        if (entity == null) {
            return DisguiseRegistry.UNKNOWN_HEAD_KEY;
        }

        return HEAD_KEYS.getOrDefault(entity.getType(), DisguiseRegistry.UNKNOWN_HEAD_KEY);
    }

    @Nullable
    public static EntityType<?> entityTypeForHead(@NotNull final String headKey) {
        return ENTITY_TYPES.get(headKey);
    }

    @NotNull
    public static Identifier texture(@NotNull final String headKey) {
        final String key = headKey.isEmpty() ? DisguiseRegistry.UNKNOWN_HEAD_KEY : headKey;
        return Identifier.fromNamespaceAndPath(NAMESPACE, "textures/gui/mob_heads/" + key + ".png");
    }
}
