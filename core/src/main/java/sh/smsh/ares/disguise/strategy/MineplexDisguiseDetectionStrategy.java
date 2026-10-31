package sh.smsh.ares.disguise.strategy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.connection.ServerType;
import sh.smsh.ares.disguise.DisguiseDetectionStrategy;
import sh.smsh.ares.disguise.self.SelfHotbarItem;
import sh.smsh.ares.disguise.self.SelfKitRule;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MineplexDisguiseDetectionStrategy implements DisguiseDetectionStrategy {

    private static final String IRON_AXE = "minecraft:iron_axe";
    private static final String STONE_SWORD = "minecraft:stone_sword";
    private static final String IRON_SWORD = "minecraft:iron_sword";

    @NotNull private static final List<SelfKitRule> SELF_KIT_RULES = List.of(
            new SelfKitRule(IRON_AXE, "skeleton", "Bone Explosion"),
            new SelfKitRule(IRON_AXE, "villagergolem", "Fissure"),
            new SelfKitRule(IRON_SWORD, "spider", "Needler"),
            new SelfKitRule(IRON_SWORD, "slime", "Slime Rocket"),
            new SelfKitRule(IRON_AXE, "squid", "Ink Shotgun"),
            new SelfKitRule(IRON_AXE, "creeper", "Sulphur Bomb"),
            new SelfKitRule(IRON_SWORD, "enderman", "Block Toss"),
            new SelfKitRule(IRON_AXE, "snowman", "Blizzard"),
            new SelfKitRule(IRON_AXE, "wolf", "Cub Tackle"),
            new SelfKitRule(IRON_AXE, "lavaslime", "Magma Blast"),
            new SelfKitRule(IRON_AXE, "witch", "Daze Potion"),
            new SelfKitRule(STONE_SWORD, "witherskeleton", "Guided Wither Skull"),
            new SelfKitRule(IRON_AXE, "zombie", "Bile Blaster"),
            new SelfKitRule(IRON_AXE, "cow", "Angry Herd"),
            new SelfKitRule(IRON_AXE, "skeletonhorse", "Bone Kick"),
            new SelfKitRule(IRON_AXE, "pig", "Bouncy Bacon"),
            new SelfKitRule(IRON_SWORD, "blaze", "Inferno"),
            new SelfKitRule(IRON_SWORD, "chicken", "Egg Blaster"),
            new SelfKitRule(IRON_AXE, "guardian", "Whirlpool Axe"),
            new SelfKitRule(IRON_SWORD, "sheep", "Static Laser"),
            new SelfKitRule(IRON_AXE, "villager", "Sonic Hurr"),
            new SelfKitRule(IRON_AXE, "goat", "Goated Hurr"),
            new SelfKitRule(IRON_AXE, "mooshroom", "Strawberry Swirl")
    );

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
            return Set.of();
        }

        final Set<String> strippedSidebar = new LinkedHashSet<>();
        for (final String line : sidebarLines) {
            final String stripped = TextFormattingUtil.stripFormatting(line);
            if (!stripped.isEmpty()) {
                strippedSidebar.add(stripped);
            }
        }

        final Set<String> participating = new LinkedHashSet<>();
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
        for (final SelfHotbarItem item : hotbar) {
            if (item.isEmpty()) {
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
