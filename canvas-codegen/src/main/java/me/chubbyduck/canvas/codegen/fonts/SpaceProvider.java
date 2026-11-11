package me.chubbyduck.canvas.codegen.fonts;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@code "type": "space"}: invisible glyphs, keyed by a one-codepoint string, with their advance in pixels.
 */
public record SpaceProvider(@NonNull Map<String, Float> advances, @Nullable Map<String, Boolean> filter) implements FontProvider {

    /**
     * Bold moves space glyphs by a whole pixel, in half-pixels.
     */
    public static final int BOLD_OFFSET = 2;

    /**
     * Codepoint to {@code {advance, boldOffset}}.
     */
    @NonNull
    public Map<Integer, int[]> glyphs() {
        final Map<Integer, int[]> glyphs = new HashMap<>();

        for (final Map.Entry<String, Float> advance : this.advances.entrySet()) {
            glyphs.put(advance.getKey().codePointAt(0), new int[]{Math.round(advance.getValue()), BOLD_OFFSET});
        }

        return glyphs;
    }
}
