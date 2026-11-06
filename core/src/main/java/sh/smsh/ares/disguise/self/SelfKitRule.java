package sh.smsh.ares.disguise.self;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class SelfKitRule {

    @NotNull private final String itemKey;
    @NotNull private final List<String> nameKeywords;
    @NotNull private final String headKey;

    public SelfKitRule(@NotNull final String itemKey, @NotNull final String headKey, @NotNull final String... nameKeywords) {
        this(itemKey, List.of(nameKeywords), headKey);
    }

    public boolean matches(@NotNull final SelfHotbarItem item) {
        if (item.isEmpty() || !itemKey.equals(item.getItemKey())) {
            return false;
        }

        if (nameKeywords.isEmpty() || item.getDisplayName().isEmpty()) {
            return false;
        }

        final String lower = item.getDisplayName().toLowerCase();
        for (final String keyword : nameKeywords) {
            if (keyword == null || keyword.isEmpty()) {
                continue;
            }

            if (!lower.contains(keyword.toLowerCase())) {
                return false;
            }
        }

        return true;
    }
}
