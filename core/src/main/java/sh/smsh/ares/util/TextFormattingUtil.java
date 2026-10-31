package sh.smsh.ares.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextFormattingUtil {

    @Nullable
    public static String stripFormatting(@Nullable final String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return text.replaceAll("(?i)§[0-9A-FK-OR]", "").trim();
    }
}
