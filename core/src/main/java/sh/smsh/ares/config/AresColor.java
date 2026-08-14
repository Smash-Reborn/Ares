package sh.smsh.ares.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.util.TextFormattingUtil;

/**
 * The 16 vanilla formatting colors ({@code §0}-{@code §f}) used by chat, nametags, and HUD.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public enum AresColor {

    BLACK(0x000000, '0'),
    DARK_BLUE(0x0000AA, '1'),
    DARK_GREEN(0x00AA00, '2'),
    DARK_AQUA(0x00AAAA, '3'),
    DARK_RED(0xAA0000, '4'),
    DARK_PURPLE(0xAA00AA, '5'),
    GOLD(0xFFAA00, '6'),
    GRAY(0xAAAAAA, '7'),
    DARK_GRAY(0x555555, '8'),
    BLUE(0x5555FF, '9'),
    GREEN(0x55FF55, 'a'),
    AQUA(0x55FFFF, 'b'),
    RED(0xFF5555, 'c'),
    LIGHT_PURPLE(0xFF55FF, 'd'),
    YELLOW(0xFFFF55, 'e'),
    WHITE(0xFFFFFF, 'f');

    private static final char SECTION_SIGN = '\u00A7';

    private final int rgb;
    private final char chatCode;

    public int getRed() {
        return (rgb >> 16) & 0xFF;
    }

    public int getGreen() {
        return (rgb >> 8) & 0xFF;
    }

    public int getBlue() {
        return rgb & 0xFF;
    }

    public int getArgb() {
        return 0xFF000000 | rgb;
    }

    @NotNull public String applyTo(@Nullable final String text) {
        return SECTION_SIGN + String.valueOf(chatCode) + TextFormattingUtil.stripFormatting(text);
    }

    @NotNull public static AresColor orDefault(@Nullable final AresColor color, @NotNull final AresColor fallback) {
        return color != null ? color : fallback;
    }
}
