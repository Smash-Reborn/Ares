package sh.smsh.ares.fabric.debug;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mineplex damage popups are text displays whose text is
 * {@code U+25B7 <amount> U+25C1} ({@code ▷ amount ◁}).
 * Those two code points are what show up as {@code â–·} and {@code â—}
 * when the same UTF-8 bytes are read as Windows-1252.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DamageIndicatorText {

    public static final int OPEN = 0x25B7;
    public static final int CLOSE = 0x25C1;

    @Nullable
    public static String damageValue(@NotNull final String text) {
        final String trimmed = text.trim();
        if (trimmed.length() < 2) {
            return null;
        }

        final int open = trimmed.codePointAt(0);
        final int close = trimmed.codePointBefore(trimmed.length());
        if (open != OPEN || close != CLOSE) {
            return null;
        }

        final int contentStart = Character.charCount(open);
        final int contentEnd = trimmed.length() - Character.charCount(close);
        if (contentEnd < contentStart) {
            return null;
        }

        final String value = trimmed.substring(contentStart, contentEnd).trim();
        return value.isEmpty() ? null : value;
    }

    @NotNull
    public static String describe(@NotNull final String text) {
        final String damage = damageValue(text);
        if (damage != null) {
            return "damage=" + damage;
        }

        return "\"" + text + "\" " + codePoints(text);
    }

    @NotNull
    private static String codePoints(@NotNull final String text) {
        final StringBuilder builder = new StringBuilder();
        text.codePoints().forEach(codePoint -> {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(String.format("U+%04X", codePoint));
        });
        return builder.toString();
    }
}
