package sh.smsh.ares.disguise.strategy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerType;
import sh.smsh.ares.disguise.DisguiseDetectionStrategy;
import sh.smsh.ares.disguise.SelfHotbarItem;
import sh.smsh.ares.disguise.SelfKitRule;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MineplexDisguiseDetectionStrategy implements DisguiseDetectionStrategy {

    /*
     * unfortunately, i couldn't figure out a good way to retrieve the players self-disguise
     * since the server doesn't ever send any sort of packet indicating what disguise you have.
     * this was the best way i could think to retrieve, at least the relative disguise based
     * on a kits equipped items (since this is immutable, although, might change between updates).
     *
     * if someone thinks of a better way to do this 1000% please replace this hacky solution.
     */

    /**
     * Example:
     * {@code new SelfKitRule(267, -1, "skeleton", "Skeleton")}
     * → iron sword (id 267), any meta, display name contains "Skeleton" → {@code skeleton.png}
     * <p>
     * {@code itemId = -1} matches any item; {@code metadata = -1} matches any damage value.
     * All keywords must appear in the stripped display name (case-insensitive).
     */
    @NotNull private static final List<SelfKitRule> SELF_KIT_RULES = Collections.unmodifiableList(Arrays.asList(
            // itemId 258 = iron axe (1.8)
            // itemId 267 = iron sword (1.8)
            new SelfKitRule(258,  "skeleton", "Bone Explosion"),
            new SelfKitRule(258,  "villagergolem", "Fissure"),
            new SelfKitRule(267,  "spider", "Needler"),
            new SelfKitRule(267,  "slime", "Slime Rocket"),
            new SelfKitRule(258,  "squid", "Ink Shotgun"),
            new SelfKitRule(258, "creeper,", "Sulphur Bomb"),
            new SelfKitRule(267, "enderman", "Block Toss"),
            new SelfKitRule(258, "snowman", "Blizzard"),
            new SelfKitRule(258, "wolf", "Cub Tackle"),
            new SelfKitRule(258, "lavaslime", "Magma Blast"),
            new SelfKitRule(258, "witch", "Daze Potion"),
            new SelfKitRule(267, "witherskeleton", "Guided Wither Skull"),
            new SelfKitRule(258, "zombie", "Bile Blaster"),
            new SelfKitRule(258, "cow", "Angry Herd"),
            new SelfKitRule(258, "skeletonhorse", "Bone Kick"),
            new SelfKitRule(258, "pig", "Bouncy Bacon"),
            new SelfKitRule(267, "blaze", "Inferno"),
            new SelfKitRule(267, "chicken", "Egg Blaster"),
            new SelfKitRule(258, "guardian", "Whirlpool Axe"),
            new SelfKitRule(267, "sheep", "Static Laser"),
            new SelfKitRule(258, "villager", "Sonic Hurr")
    ));

    @Override
    public boolean supports(@NotNull final ServerType serverType) {
        return serverType == ServerType.MINEPLEX;
    }

    @Override
    @NotNull
    public Set<String> resolveParticipatingPlayers(
            @NotNull final Collection<String> tabNames,
            @NotNull final Collection<String> sidebarLines) {
        if (tabNames.isEmpty() || sidebarLines.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<String> strippedSidebar = new LinkedHashSet<String>();
        for (final String line : sidebarLines) {
            final String stripped = TextFormattingUtil.stripFormatting(line);
            if (!stripped.isEmpty()) {
                strippedSidebar.add(stripped);
            }
        }

        if (strippedSidebar.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<String> participating = new LinkedHashSet<String>();
        for (final String tabName : tabNames) {
            if (tabName == null || tabName.isEmpty()) {
                continue;
            }

            final String strippedName = TextFormattingUtil.stripFormatting(tabName);
            if (strippedName.isEmpty()) {
                continue;
            }

            for (final String sidebarLine : strippedSidebar) {
                if (sidebarLine.contains(strippedName)) {
                    participating.add(strippedName);
                    break;
                }
            }
        }

        return participating;
    }

    @Override
    @Nullable
    public String matchPlayerFromCustomName(
            @Nullable final String customName,
            @NotNull final Set<String> participatingPlayers) {
        if (customName == null || customName.isEmpty() || participatingPlayers.isEmpty()) {
            return null;
        }

        final String stripped = TextFormattingUtil.stripFormatting(customName);
        if (stripped.isEmpty()) {
            return null;
        }

        for (final String playerName : participatingPlayers) {
            if (stripped.equals(playerName)) {
                return playerName;
            }
        }

        return null;
    }

    @Override
    @Nullable
    public String resolveSelfHeadKey(@NotNull final List<SelfHotbarItem> hotbar) {
        if (hotbar.isEmpty() || SELF_KIT_RULES.isEmpty()) {
            return null;
        }

        for (final SelfHotbarItem item : hotbar) {
            if (item.getItemId() < 0) {
                continue;
            }
            for (final SelfKitRule rule : SELF_KIT_RULES) {
                if (rule.matches(item)) {
                    return rule.getHeadKey();
                }
            }
        }

        return null;
    }
}
