package sh.smsh.ares.fabric.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.disguise.DisguiseBinding;
import sh.smsh.ares.disguise.DisguiseRegistry;
import sh.smsh.ares.feature.impl.DisguiseDetectionFeature;

import java.util.*;

/**
 * An invisible squid spawned after a disguise and teleported to that disguise's passenger height is that disguise's hitbox.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisguiseSquids {

    private static final double XZ_RANGE = 1.0D;
    private static final double Y_LENIENCY_MIN = -2.0D;
    private static final double Y_LENIENCY_MAX = 2.0D;

    private static final Map<Integer, Long> spawnOrder = new HashMap<>();
    private static final Set<Integer> unclaimedSquids = new HashSet<>();
    private static long nextSpawnOrder;

    public static void noteSpawn(final int entityId) {
        if (!DisguiseDetectionFeature.shouldApply()) {
            return;
        }

        spawnOrder.put(entityId, nextSpawnOrder++);
        final Minecraft minecraft = Minecraft.getInstance();
        final Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(entityId);
        if (entity != null && entity.getType() == EntityTypes.SQUID) {
            unclaimedSquids.add(entityId);
        }
    }

    public static void observe(@Nullable final Entity entity) {
        if (entity == null || entity.getType() != EntityTypes.SQUID || !spawnOrder.containsKey(entity.getId())) {
            return;
        }

        if (!DisguiseRegistry.isOwnedSquid(entity.getId())) {
            unclaimedSquids.add(entity.getId());
        }
    }

    public static void tryClaim() {
        if (!DisguiseDetectionFeature.shouldApply() || unclaimedSquids.isEmpty()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        final List<Claim> claims = new ArrayList<>();
        for (final int squidId : unclaimedSquids) {
            final Entity squid = minecraft.level.getEntity(squidId);
            if (squid == null || squid.getType() != EntityTypes.SQUID || !squid.isInvisible()) {
                continue;
            }

            final Claim claim = nearestDisguise(minecraft, squid);
            if (claim != null) {
                claims.add(claim);
            }
        }

        claims.sort(Comparator.comparingDouble(Claim::distanceSqr));
        final Set<Integer> usedDisguises = new HashSet<>();
        final Set<Integer> claimedSquids = new HashSet<>();
        for (final Claim claim : claims) {
            if (!usedDisguises.add(claim.disguiseEntityId()) || !claimedSquids.add(claim.squidEntityId())) {
                continue;
            }
            if (DisguiseRegistry.hasOwnedSquid(claim.disguiseEntityId())) {
                continue;
            }

            DisguiseRegistry.setOwnedSquid(claim.disguiseEntityId(), claim.squidEntityId());
            unclaimedSquids.remove(claim.squidEntityId());
        }
    }

    public static void forget(final int[] entityIds) {
        for (final int entityId : entityIds) {
            spawnOrder.remove(entityId);
            unclaimedSquids.remove(entityId);
            DisguiseRegistry.clearOwnedSquid(entityId);
        }
    }

    public static void clear() {
        spawnOrder.clear();
        unclaimedSquids.clear();
        nextSpawnOrder = 0L;
    }

    @Nullable
    private static Claim nearestDisguise(@NotNull final Minecraft minecraft, @NotNull final Entity squid) {
        final Long squidOrder = spawnOrder.get(squid.getId());
        if (squidOrder == null || minecraft.level == null) {
            return null;
        }

        Claim best = null;
        for (final DisguiseBinding binding : DisguiseRegistry.getAll()) {
            if (binding.isSelfInferred() || DisguiseRegistry.hasOwnedSquid(binding.getEntityId())) {
                continue;
            }

            final Long disguiseOrder = spawnOrder.get(binding.getEntityId());
            if (disguiseOrder == null || squidOrder <= disguiseOrder) {
                continue;
            }

            final Entity disguise = minecraft.level.getEntity(binding.getEntityId());
            if (disguise == null) {
                continue;
            }

            final double dx = squid.getX() - disguise.getX();
            final double dy = squid.getY() - expectedPassengerY(disguise, squid);
            final double dz = squid.getZ() - disguise.getZ();
            if ((dx * dx) + (dz * dz) > XZ_RANGE * XZ_RANGE || dy < Y_LENIENCY_MIN || dy > Y_LENIENCY_MAX) {
                continue;
            }

            final double distanceSqr = (dx * dx) + (dy * dy) + (dz * dz);
            if (best == null || distanceSqr < best.distanceSqr()) {
                best = new Claim(binding.getEntityId(), squid.getId(), distanceSqr);
            }
        }

        return best;
    }

    /**
     * Feet height vanilla uses when placing a passenger, which is where the hitbox squid is teleported.
     * The seat comes from the disguise; the squid's own vehicle attachment is then subtracted, matching {@code positionRider}.
     */
    private static double expectedPassengerY(@NotNull final Entity disguise, @NotNull final Entity squid) {
        final Vec3 seat = disguise.getPassengerRidingPosition(squid);
        final Vec3 attachment = squid.getVehicleAttachmentPoint(disguise);
        return seat.y - attachment.y;
    }

    private record Claim(int disguiseEntityId, int squidEntityId, double distanceSqr) { }
}
