package sh.smsh.ares.disguise;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.smsh.ares.util.TextFormattingUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Reads stock counts from sidebar lines.
 * Free-for-all lines are {@code <stock> <player>}. Teams lines lead with a team square, then the same pair.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SidebarStocks {

    @NotNull
    public static Map<String, Integer> parse(@NotNull final Collection<String> lines) {
        final Map<String, Integer> stocks = new HashMap<>();
        for (final String line : lines) {
            readLine(stocks, line);
        }
        return stocks;
    }

    @Nullable
    public static Integer get(@NotNull final Map<String, Integer> stocks, @NotNull final String playerName) {
        if (playerName.isEmpty()) {
            return null;
        }
        return stocks.get(playerName.toLowerCase(Locale.ROOT));
    }

    private static void readLine(@NotNull final Map<String, Integer> stocks, @Nullable final String line) {
        final String stripped = TextFormattingUtil.stripFormatting(line);
        if (stripped == null || stripped.isEmpty()) {
            return;
        }

        final String[] parts = stripped.split("\\s+");
        if (parts.length < 2) {
            return;
        }

        for (int index = 0; index < parts.length - 1; index++) {
            final Integer stock = stockToken(parts[index], index == 0);
            if (stock == null) {
                continue;
            }
            put(stocks, join(parts, index + 1), stock);
            return;
        }
    }

    private static void put(
            @NotNull final Map<String, Integer> stocks,
            @NotNull final String playerName,
            final int stock) {
        if (playerName.isEmpty() || stocks.containsKey(playerName.toLowerCase(Locale.ROOT))) {
            return;
        }
        stocks.put(playerName.toLowerCase(Locale.ROOT), stock);
    }

    @NotNull
    private static String join(@NotNull final String[] parts, final int from) {
        final StringBuilder builder = new StringBuilder();
        for (int index = from; index < parts.length; index++) {
            if (index > from) {
                builder.append(' ');
            }
            builder.append(parts[index]);
        }
        return builder.toString();
    }

    @Nullable
    private static Integer stockToken(@NotNull final String token, final boolean allowDecorated) {
        if (token.isEmpty()) {
            return null;
        }

        int start = 0;
        if (allowDecorated && !Character.isDigit(token.charAt(0))) {
            if (token.charAt(0) == '-' || token.charAt(0) == '+') {
                return null;
            }
            while (start < token.length() && !Character.isDigit(token.charAt(start))) {
                start++;
            }
            if (start == 0 || start == token.length()) {
                return null;
            }
        }

        for (int index = start; index < token.length(); index++) {
            if (!Character.isDigit(token.charAt(index))) {
                return null;
            }
        }
        if (start == token.length()) {
            return null;
        }

        try {
            return Integer.parseInt(token.substring(start));
        } catch (final NumberFormatException ex) {
            return null;
        }
    }
}
