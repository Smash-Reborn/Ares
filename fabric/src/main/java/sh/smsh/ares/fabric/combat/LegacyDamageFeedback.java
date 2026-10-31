package sh.smsh.ares.fabric.combat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import sh.smsh.ares.feature.impl.LegacyDamageSoundFeature;

/**
 * A hit is the hurt-tilt packet and a health drop, in either order, close together.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LegacyDamageFeedback {

    private static final long PAIR_WINDOW_NANOS = 500_000_000L;

    private static final float HEALTH_EPSILON = 0.01F;

    private static long hurtAtNanos;
    private static long healthDropAtNanos;
    private static float trackedHealth = -1.0F;

    public static void onHurtAnimation(final int entityId) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.getId() != entityId) {
            return;
        }

        hurtAtNanos = System.nanoTime();
        tryPlay(player);
    }

    public static void onHealthPacket(final float previousHealth, final float newHealth) {
        final float baseline = trackedHealth >= 0.0F ? trackedHealth : previousHealth;
        if (newHealth >= baseline - HEALTH_EPSILON) {
            return;
        }

        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        healthDropAtNanos = System.nanoTime();
        tryPlay(player);
    }

    public static void captureHealth() {
        final LocalPlayer player = Minecraft.getInstance().player;
        trackedHealth = player == null ? -1.0F : player.getHealth();
    }

    private static void tryPlay(final LocalPlayer player) {
        if (!LegacyDamageSoundFeature.shouldApply() || hurtAtNanos == 0L || healthDropAtNanos == 0L) {
            return;
        }

        if (Math.abs(hurtAtNanos - healthDropAtNanos) > PAIR_WINDOW_NANOS) {
            return;
        }

        hurtAtNanos = 0L;
        healthDropAtNanos = 0L;
        trackedHealth = player.getHealth();

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        minecraft.level.playLocalSound(
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.GENERIC_HURT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F,
                false);
    }
}
