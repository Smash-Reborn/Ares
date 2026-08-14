package sh.smsh.ares.disguise;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * One self-kit match rule: optional item id/metadata + display-name keywords → head texture key.
 * <p>
 * Use {@code itemId = -1} / {@code metadata = -1} to ignore that field.
 * Every keyword must appear in the stripped display name (case-insensitive).
 */
@Getter
@RequiredArgsConstructor
public final class SelfKitRule {

    private final int itemId;
    private final int metadata;
    @NotNull private final List<String> nameKeywords;
    @NotNull private final String headKey;

    public SelfKitRule(
            final int itemId,
            final int metadata,
            @NotNull final String headKey,
            @NotNull final String... nameKeywords) {

        this(itemId, metadata, Collections.unmodifiableList(Arrays.asList(nameKeywords)), headKey);
    }

    public SelfKitRule(
            final int itemId,
            @NotNull final String headKey,
            @NotNull final String... nameKeywords) {

        this(itemId, -1, headKey, nameKeywords);
    }

    public boolean matches(@NotNull final SelfHotbarItem item) {
        if (item.getItemId() < 0) {
            return false;
        }
        if (itemId >= 0 && item.getItemId() != itemId) {
            return false;
        }
        if (metadata >= 0 && item.getMetadata() != metadata) {
            return false;
        }
        if (nameKeywords.isEmpty()) {
            return false;
        }

        final String name = item.getDisplayName();
        if (name.isEmpty()) {
            return false;
        }

        final String lower = name.toLowerCase();
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
