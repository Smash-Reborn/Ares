package sh.smsh.ares.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextFormattingUtil {

    private static final char SECTION_SIGN = '\u00A7';

    @NotNull
    public static String stripFormatting(@Nullable final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        final StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if ((c == SECTION_SIGN || c == '&') && i + 1 < input.length()) {
                i++;
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }
}
