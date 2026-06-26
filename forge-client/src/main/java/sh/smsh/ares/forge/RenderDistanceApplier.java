package sh.smsh.ares.forge;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import sh.smsh.ares.gate.ServerGate;
import sh.smsh.ares.settings.ModSettings;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenderDistanceApplier {

    public static void apply(@NotNull final Entity entity) {
        if (!ServerGate.isAllowed()) {
            return;
        }

        if (!ModSettings.isExtendedRenderDistanceEnabled()) {
            return;
        }

        if (!isEntityAllowedToBeRenderExtended(entity)) {
            return;
        }

        entity.renderDistanceWeight = ModSettings.getRenderDistanceWeight();
    }

    /*
     * not every entity in 1.8 ssm needs to be rendered from far away.
     * we also want to minimize the amount we are actually allowing to
     * be rendered since renderWeights exist to limit the amount of
     * render processing the client does, in order to reduce load.
     */
    @ApiStatus.Internal
    private static boolean isEntityAllowedToBeRenderExtended(@NotNull final Entity entity) {
        switch (EntityList.getEntityID(entity)) {
            // taken from EntityList.class
            case 1: // item
            case 7: // egg
            case 10: // arrow
            case 11: // snowball
            case 12: // fireball
            case 13: // small fireball
            case 14: // enderpearl
            case 16: // potion
            case 19: // wither skull
            case 21: // falling block
            case 50: // creeper
            case 51: // skeleton
            case 52: // spider
            case 53: // giant
            case 54: // zombie
            case 55: // slime
            case 56: // ghast
            case 57: // zombie pigman
            case 58: // enderman
            case 59: // cave spider
            case 60: // silverfish
            case 61: // blaze
            case 62: // magma cube
            case 65: // bat
            case 66: // witch
            case 67: // endermite
            case 68: // guardian
            case 90: // pig
            case 91: // shemp
            case 92: // cow
            case 93: // chicken
            case 94: // squid
            case 95: // wolf
            case 96: // mooshroom
            case 97: // snowman
            case 98: // ocelot
            case 99: // iron gholem
            case 100: // horse
            case 101: // rabbit
            case 120: // villager
                return true;
            default:
                return false;
        }
    }
}
