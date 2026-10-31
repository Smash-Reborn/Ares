package sh.smsh.ares.fabric.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.feature.impl.DisableAmbientDisguiseSoundsFeature;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseAmbientSounds {

    private static final double NEARBY_DISTANCE_SQR = 4.0D * 4.0D;

    /**
     * Sound-event paths for each disguise. A step such as {@code entity.spider.step}
     * matches the {@code entity.spider} prefix, including when the packet's
     * {@link SoundEvent} is not the same instance as the registry constant.
     */
    private static final Map<String, List<String>> SOUND_PREFIXES = Map.ofEntries(
            Map.entry("spider", List.of("entity.spider")),
            Map.entry("cavespider", List.of("entity.cave_spider", "entity.spider")),
            Map.entry("skeleton", List.of("entity.skeleton")),
            Map.entry("witherskeleton", List.of("entity.wither_skeleton")),
            Map.entry("zombie", List.of("entity.zombie")),
            Map.entry("enderman", List.of("entity.enderman")),
            Map.entry("blaze", List.of("entity.blaze")),
            Map.entry("creeper", List.of("entity.creeper")),
            Map.entry("slime", List.of("entity.slime")),
            Map.entry("lavaslime", List.of("entity.magma_cube")),
            Map.entry("squid", List.of("entity.squid")),
            Map.entry("witch", List.of("entity.witch")),
            Map.entry("villager", List.of("entity.villager")),
            Map.entry("villagergolem", List.of("entity.iron_golem")),
            Map.entry("snowman", List.of("entity.snow_golem")),
            Map.entry("sheep", List.of("entity.sheep")),
            Map.entry("guardian", List.of("entity.guardian")),
            Map.entry("skeletonhorse", List.of("entity.skeleton_horse")),
            Map.entry("zombiehorse", List.of("entity.zombie_horse")),
            Map.entry("donkey", List.of("entity.donkey")),
            Map.entry("chicken", List.of("entity.chicken")),
            Map.entry("pig", List.of("entity.pig")),
            Map.entry("wolf", List.of("entity.wolf")),
            Map.entry("cow", List.of("entity.cow")),
            Map.entry("goat", List.of("entity.goat")),
            Map.entry("mooshroom", List.of("entity.mooshroom", "entity.cow")),
            Map.entry("phantom", List.of("entity.phantom")),
            Map.entry("polarbear", List.of("entity.polar_bear")),
            Map.entry("breeze", List.of("entity.breeze")),
            Map.entry("evoker", List.of("entity.evoker")),
            Map.entry("hoglin", List.of("entity.hoglin")),
            Map.entry("drowned", List.of("entity.drowned")),
            Map.entry("llama", List.of("entity.llama")),
            Map.entry("pillager", List.of("entity.pillager"))
    );

    public static boolean shouldCancelPlayedBy(@NotNull final Entity entity, @NotNull final SoundEvent sound) {
        final LocalPlayer self = localPlayer();
        if (self == null) {
            return false;
        }

        if (entity == self) {
            return matches(selfHeadKey(self), sound);
        }

        return isOwnDisguiseMob(self, entity) && matches(DisguiseHeads.resolveHeadKey(entity), sound);
    }

    public static boolean shouldCancelEntitySound(@NotNull final Holder<SoundEvent> sound, final int entityId) {
        final LocalPlayer self = localPlayer();
        if (self == null) {
            return false;
        }

        final SoundEvent event = sound.value();
        if (entityId == self.getId()) {
            return matches(selfHeadKey(self), event);
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(entityId);
        if (entity == null) {
            final DisguiseBinding binding = DisguiseRegistry.getByPlayerName(self.getName().getString());
            return binding != null && binding.getEntityId() == entityId && matches(binding.getHeadKey(), event);
        }

        return shouldCancelPlayedBy(entity, event);
    }

    public static boolean shouldCancelPositionalSound(
            @NotNull final Holder<SoundEvent> sound,
            final double x,
            final double y,
            final double z) {

        final LocalPlayer self = localPlayer();
        if (self == null) {
            return false;
        }

        if (!matches(selfHeadKey(self), sound.value())) {
            return false;
        }

        return self.distanceToSqr(new Vec3(x, y, z)) <= NEARBY_DISTANCE_SQR;
    }

    private static boolean isOwnDisguiseMob(@NotNull final LocalPlayer self, @NotNull final Entity entity) {
        if (!(entity instanceof LivingEntity) || entity instanceof Player || entity.getCustomName() == null) {
            return false;
        }

        final String name = TextFormattingUtil.stripFormatting(entity.getCustomName().getString());
        return name.equalsIgnoreCase(self.getName().getString());
    }

    private static String selfHeadKey(@NotNull final LocalPlayer self) {
        final DisguiseBinding binding = DisguiseRegistry.getByPlayerName(self.getName().getString());
        return binding == null ? "" : binding.getHeadKey();
    }

    private static boolean matches(@NotNull final String headKey, @NotNull final SoundEvent sound) {
        final List<String> prefixes = SOUND_PREFIXES.get(headKey);
        if (prefixes == null || prefixes.isEmpty()) {
            return false;
        }

        final Identifier location = sound.location();
        final String path = location.getPath();
        for (final String prefix : prefixes) {
            if (path.equals(prefix) || path.startsWith(prefix + ".")) {
                return true;
            }
        }
        return false;
    }

    private static LocalPlayer localPlayer() {
        if (!DisableAmbientDisguiseSoundsFeature.shouldApply()) {
            return null;
        }

        return Minecraft.getInstance().player;
    }
}
