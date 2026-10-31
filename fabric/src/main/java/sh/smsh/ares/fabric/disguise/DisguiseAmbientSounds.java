package sh.smsh.ares.fabric.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.feature.impl.DisableAmbientDisguiseSoundsFeature;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseAmbientSounds {

    private static final double NEARBY_DISTANCE_SQR = 2.0D * 2.0D;

    /**
     * Step and ambient sounds for each disguise head key. Spider walk is {@code entity.spider.step}.
     */
    private static final Map<String, Set<SoundEvent>> BY_HEAD_KEY = Map.ofEntries(
            Map.entry("spider", sounds(SoundEvents.SPIDER_STEP, SoundEvents.SPIDER_AMBIENT)),
            Map.entry("cavespider", sounds(SoundEvents.SPIDER_STEP, SoundEvents.SPIDER_AMBIENT)),
            Map.entry("skeleton", sounds(SoundEvents.SKELETON_STEP, SoundEvents.SKELETON_AMBIENT)),
            Map.entry("witherskeleton", sounds(SoundEvents.WITHER_SKELETON_STEP, SoundEvents.WITHER_SKELETON_AMBIENT)),
            Map.entry("zombie", sounds(SoundEvents.ZOMBIE_STEP, SoundEvents.ZOMBIE_AMBIENT)),
            Map.entry("enderman", sounds(SoundEvents.ENDERMAN_AMBIENT, SoundEvents.ENDERMAN_STARE, SoundEvents.ENDERMAN_SCREAM)),
            Map.entry("blaze", sounds(SoundEvents.BLAZE_AMBIENT)),
            Map.entry("creeper", sounds(SoundEvents.CREEPER_PRIMED)),
            Map.entry("slime", sounds(SoundEvents.SLIME_SQUISH, SoundEvents.SLIME_SQUISH_SMALL, SoundEvents.SLIME_JUMP, SoundEvents.SLIME_JUMP_SMALL)),
            Map.entry("lavaslime", sounds(SoundEvents.MAGMA_CUBE_SQUISH, SoundEvents.MAGMA_CUBE_SQUISH_SMALL)),
            Map.entry("squid", sounds(SoundEvents.SQUID_AMBIENT)),
            Map.entry("witch", sounds(SoundEvents.WITCH_AMBIENT)),
            Map.entry("villager", sounds(SoundEvents.VILLAGER_AMBIENT)),
            Map.entry("villagergolem", sounds(SoundEvents.IRON_GOLEM_STEP)),
            Map.entry("snowman", sounds(SoundEvents.SNOW_GOLEM_AMBIENT)),
            Map.entry("sheep", sounds(SoundEvents.SHEEP_STEP, SoundEvents.SHEEP_AMBIENT)),
            Map.entry("guardian", sounds(SoundEvents.GUARDIAN_AMBIENT, SoundEvents.GUARDIAN_AMBIENT_LAND)),
            Map.entry("skeletonhorse", sounds(SoundEvents.SKELETON_HORSE_AMBIENT, SoundEvents.SKELETON_HORSE_STEP_WATER)),
            Map.entry("zombiehorse", sounds(SoundEvents.ZOMBIE_HORSE_AMBIENT)),
            Map.entry("donkey", sounds(SoundEvents.DONKEY_AMBIENT)),
            Map.entry("chicken", sounds(SoundEvents.CHICKEN_STEP.value())),
            Map.entry("pig", sounds(SoundEvents.PIG_STEP.value())),
            Map.entry("wolf", sounds(SoundEvents.WOLF_STEP.value()))
    );

    public static boolean shouldCancelEntitySound(@NotNull final Holder<SoundEvent> sound, final int entityId) {
        final LocalPlayer player = localDisguisedPlayer();
        if (player == null || !matchesDisguise(player, sound.value())) {
            return false;
        }

        if (entityId == player.getId()) {
            return true;
        }

        final DisguiseBinding binding = DisguiseRegistry.getByPlayerName(player.getName().getString());
        return binding != null && binding.getEntityId() == entityId;
    }

    public static boolean shouldCancelPositionalSound(
            @NotNull final Holder<SoundEvent> sound,
            final double x,
            final double y,
            final double z) {

        final LocalPlayer player = localDisguisedPlayer();
        if (player == null || !matchesDisguise(player, sound.value())) {
            return false;
        }

        return player.distanceToSqr(new Vec3(x, y, z)) <= NEARBY_DISTANCE_SQR;
    }

    private static boolean matchesDisguise(@NotNull final LocalPlayer player, @NotNull final SoundEvent sound) {
        final DisguiseBinding binding = DisguiseRegistry.getByPlayerName(player.getName().getString());
        if (binding == null) {
            return false;
        }

        final Set<SoundEvent> sounds = BY_HEAD_KEY.get(binding.getHeadKey());
        return sounds != null && sounds.contains(sound);
    }

    private static LocalPlayer localDisguisedPlayer() {
        if (!DisableAmbientDisguiseSoundsFeature.shouldApply()) {
            return null;
        }

        return Minecraft.getInstance().player;
    }

    private static Set<SoundEvent> sounds(final SoundEvent... events) {
        return Set.of(events);
    }
}
