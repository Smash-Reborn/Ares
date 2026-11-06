package sh.smsh.ares.fabric.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.chicken.ChickenSoundVariant;
import net.minecraft.world.entity.animal.cow.CowSoundVariant;
import net.minecraft.world.entity.animal.pig.PigSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.feature.impl.DisableAmbientDisguiseSoundsFeature;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseAmbientSounds {

    private static final Map<EntityType<?>, Set<Identifier>> DISGUISE_SOUNDS = Map.ofEntries(
            Map.entry(EntityTypes.SKELETON, step(SoundEvents.SKELETON_STEP)),
            Map.entry(EntityTypes.IRON_GOLEM, step(SoundEvents.IRON_GOLEM_STEP)),
            Map.entry(EntityTypes.SPIDER, step(SoundEvents.SPIDER_STEP)),
            Map.entry(EntityTypes.WOLF, wolfSteps(SoundEvents.WOLF_SOUNDS)),
            Map.entry(EntityTypes.WITHER_SKELETON, step(SoundEvents.WITHER_SKELETON_STEP)),
            Map.entry(EntityTypes.ZOMBIE, step(SoundEvents.ZOMBIE_STEP)),
            Map.entry(EntityTypes.COW, cowSteps(SoundEvents.COW_SOUNDS)),
            Map.entry(EntityTypes.PIG, pigSteps(SoundEvents.PIG_SOUNDS)),
            Map.entry(EntityTypes.CHICKEN, chickenSteps(SoundEvents.CHICKEN_SOUNDS)),
            Map.entry(EntityTypes.SHEEP, step(SoundEvents.SHEEP_STEP)),
            Map.entry(EntityTypes.GOAT, step(SoundEvents.GOAT_STEP)),
            Map.entry(EntityTypes.MOOSHROOM, cowSteps(SoundEvents.COW_SOUNDS)),
            Map.entry(EntityTypes.POLAR_BEAR, step(SoundEvents.POLAR_BEAR_STEP)),
            Map.entry(EntityTypes.HOGLIN, step(SoundEvents.HOGLIN_STEP)),
            Map.entry(EntityTypes.DROWNED, step(SoundEvents.DROWNED_STEP)),
            Map.entry(EntityTypes.LLAMA, step(SoundEvents.LLAMA_STEP))
    );

    public static boolean shouldCancel(@NotNull final Identifier soundId) {
        if (!DisableAmbientDisguiseSoundsFeature.shouldApply()) {
            return false;
        }

        for (final Map.Entry<EntityType<?>, Set<Identifier>> entry : DISGUISE_SOUNDS.entrySet()) {
            if (!entry.getValue().contains(soundId)) {
                continue;
            }

            if (disguiseUses(entry.getKey())) {
                return true;
            }
        }

        return false;
    }

    private static boolean disguiseUses(@NotNull final EntityType<?> type) {
        for (final DisguiseBinding binding : DisguiseRegistry.getAll()) {
            if (type.equals(DisguiseHeads.entityTypeForHead(binding.getHeadKey()))) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private static Set<Identifier> step(@NotNull final SoundEvent sound) {
        return Set.of(sound.location());
    }

    /**
     * Each cow sound set has its own step sound, such as {@code entity.cow.step} and {@code entity.cow_moody.step}.
     */
    @NotNull
    private static Set<Identifier> cowSteps(@NotNull final Map<?, CowSoundVariant> sounds) {
        final Set<Identifier> steps = new HashSet<>();
        for (final CowSoundVariant variant : sounds.values()) {
            add(steps, variant.stepSound());
        }
        return Set.copyOf(steps);
    }

    /**
     * Adult variants share one step sound. The baby set has its own.
     */
    @NotNull
    private static Set<Identifier> pigSteps(@NotNull final Map<?, PigSoundVariant> sounds) {
        final Set<Identifier> steps = new HashSet<>();
        for (final PigSoundVariant variant : sounds.values()) {
            add(steps, variant.adultSounds().stepSound());
            add(steps, variant.babySounds().stepSound());
        }
        return Set.copyOf(steps);
    }

    @NotNull
    private static Set<Identifier> chickenSteps(@NotNull final Map<?, ChickenSoundVariant> sounds) {
        final Set<Identifier> steps = new HashSet<>();
        for (final ChickenSoundVariant variant : sounds.values()) {
            add(steps, variant.adultSounds().stepSound());
            add(steps, variant.babySounds().stepSound());
        }
        return Set.copyOf(steps);
    }

    @NotNull
    private static Set<Identifier> wolfSteps(@NotNull final Map<?, WolfSoundVariant> sounds) {
        final Set<Identifier> steps = new HashSet<>();
        for (final WolfSoundVariant variant : sounds.values()) {
            add(steps, variant.adultSounds().stepSound());
            add(steps, variant.babySounds().stepSound());
        }
        return Set.copyOf(steps);
    }

    private static void add(@NotNull final Set<Identifier> steps, @NotNull final Holder<SoundEvent> sound) {
        steps.add(sound.value().location());
    }
}
