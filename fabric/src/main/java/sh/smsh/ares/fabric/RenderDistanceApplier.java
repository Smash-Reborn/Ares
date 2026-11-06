package sh.smsh.ares.fabric;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.feature.impl.ExtendedRenderDistanceFeature;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenderDistanceApplier {

    /*
     * Same set the 1.8 applier allowed by numeric id: projectiles and
     * living entities used in Smash disguises, not players or armor stands.
     */
    private static final Set<EntityType<?>> EXTENDED_TYPES = Set.of(
            EntityTypes.ITEM,
            EntityTypes.EGG,
            EntityTypes.ARROW,
            EntityTypes.SPECTRAL_ARROW,
            EntityTypes.TRIDENT,
            EntityTypes.SNOWBALL,
            EntityTypes.FIREBALL,
            EntityTypes.SMALL_FIREBALL,
            EntityTypes.ENDER_PEARL,
            EntityTypes.SPLASH_POTION,
            EntityTypes.LINGERING_POTION,
            EntityTypes.WITHER_SKULL,
            EntityTypes.ITEM_DISPLAY,
            EntityTypes.BLOCK_DISPLAY,
            EntityTypes.TEXT_DISPLAY,
            EntityTypes.WIND_CHARGE,
            EntityTypes.BREEZE_WIND_CHARGE,
            EntityTypes.LLAMA_SPIT,
            EntityTypes.ARMOR_STAND,
            EntityTypes.FALLING_BLOCK,
            EntityTypes.CREEPER,
            EntityTypes.SKELETON,
            EntityTypes.SPIDER,
            EntityTypes.GIANT,
            EntityTypes.ZOMBIE,
            EntityTypes.SLIME,
            EntityTypes.GHAST,
            EntityTypes.ZOMBIFIED_PIGLIN,
            EntityTypes.ENDERMAN,
            EntityTypes.CAVE_SPIDER,
            EntityTypes.SILVERFISH,
            EntityTypes.BLAZE,
            EntityTypes.MAGMA_CUBE,
            EntityTypes.BAT,
            EntityTypes.WITCH,
            EntityTypes.ENDERMITE,
            EntityTypes.GUARDIAN,
            EntityTypes.PIG,
            EntityTypes.SHEEP,
            EntityTypes.COW,
            EntityTypes.CHICKEN,
            EntityTypes.SQUID,
            EntityTypes.WOLF,
            EntityTypes.MOOSHROOM,
            EntityTypes.SNOW_GOLEM,
            EntityTypes.OCELOT,
            EntityTypes.IRON_GOLEM,
            EntityTypes.HORSE,
            EntityTypes.RABBIT,
            EntityTypes.VILLAGER,
            EntityTypes.ALLAY,
            EntityTypes.ARMADILLO,
            EntityTypes.AXOLOTL,
            EntityTypes.BEE,
            EntityTypes.BOGGED,
            EntityTypes.BREEZE,
            EntityTypes.CAMEL,
            EntityTypes.CAMEL_HUSK,
            EntityTypes.CAT,
            EntityTypes.COD,
            EntityTypes.COPPER_GOLEM,
            EntityTypes.CREAKING,
            EntityTypes.DOLPHIN,
            EntityTypes.DROWNED,
            EntityTypes.EVOKER,
            EntityTypes.FOX,
            EntityTypes.FROG,
            EntityTypes.HAPPY_GHAST,
            EntityTypes.GLOW_SQUID,
            EntityTypes.GOAT,
            EntityTypes.HOGLIN,
            EntityTypes.HUSK,
            EntityTypes.ILLUSIONER,
            EntityTypes.LLAMA,
            EntityTypes.NAUTILUS,
            EntityTypes.PANDA,
            EntityTypes.PARCHED,
            EntityTypes.PARROT,
            EntityTypes.PHANTOM,
            EntityTypes.PIGLIN,
            EntityTypes.PIGLIN_BRUTE,
            EntityTypes.PILLAGER,
            EntityTypes.POLAR_BEAR,
            EntityTypes.PUFFERFISH,
            EntityTypes.RAVAGER,
            EntityTypes.SALMON,
            EntityTypes.SHULKER,
            EntityTypes.SNIFFER,
            EntityTypes.STRAY,
            EntityTypes.STRIDER,
            EntityTypes.SULFUR_CUBE,
            EntityTypes.TADPOLE,
            EntityTypes.TRADER_LLAMA,
            EntityTypes.TROPICAL_FISH,
            EntityTypes.TURTLE,
            EntityTypes.VEX,
            EntityTypes.VINDICATOR,
            EntityTypes.WANDERING_TRADER,
            EntityTypes.WARDEN,
            EntityTypes.ZOGLIN,
            EntityTypes.ZOMBIE_NAUTILUS
    );

    public static double viewScaleFor(@NotNull final Entity entity) {
        if (shouldExtend(entity)) {
            return ExtendedRenderDistanceFeature.getRenderDistanceWeight();
        }

        return Entity.getViewScale();
    }

    public static double scaleDistanceConstant(@NotNull final Entity entity, final double vanillaConstant) {
        if (!shouldExtend(entity)) {
            return vanillaConstant;
        }

        return vanillaConstant * ExtendedRenderDistanceFeature.getRenderDistanceWeight();
    }

    private static boolean shouldExtend(@NotNull final Entity entity) {
        return ExtendedRenderDistanceFeature.shouldApply() && EXTENDED_TYPES.contains(entity.getType());
    }
}
