package me.chubbyduck.canvas.core.atlas;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.chubbyduck.canvas.fonts.VanillaFontMetrics;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Font-aware text advances, with vanilla metrics and a five-pixel fallback.
 */
@NullMarked
public final class TextMetrics {

    /**
     * Legacy replacement character; text is now preserved.
     */
    public static final char REPLACEMENT = '?';

    public static final Key DEFAULT_FONT = Key.key("minecraft", "default");
    public static final int UNKNOWN_ADVANCE = 5;
    private static final Map<Key, Map<Integer, Integer>> CUSTOM_ADVANCES = new ConcurrentHashMap<>();
    private static final int FIRST = 0x20;
    private static final int LAST = 0x7E;
    private TextMetrics() {
    }

    /**
     * Registers a non-bold advance (including spacing) for one Unicode code point in a font.
     * Re-registering replaces the previous value. Register during bootstrap, before measuring or rendering UIs.
     * A null font means {@code minecraft:default}. Bold adds one pixel to this advance.
     */
    public static void registerWidth(final @Nullable Key font, final int codepoint, final int width) {
        if (!Character.isValidCodePoint(codepoint) || (codepoint >= 0xD800 && codepoint <= 0xDFFF)) {
            throw new IllegalArgumentException("Expected a Unicode scalar value: " + codepoint);
        }
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative");
        }
        CUSTOM_ADVANCES.computeIfAbsent(font == null ? DEFAULT_FONT : font, ignored -> new ConcurrentHashMap<>())
            .put(codepoint, width);
    }

    /**
     * Whether the codepoint is printable ASCII, which Canvas's offset text fonts can draw.
     */
    public static boolean isSupported(final int codepoint) {
        return codepoint >= FIRST && codepoint <= LAST;
    }

    /**
     * Preserves Unicode text, including supplementary code points.
     */
    public static String sanitize(final String text) {
        return text;
    }

    public static int advance(final int codepoint, final boolean bold) {
        return advance(null, codepoint, bold);
    }

    /**
     * Uses a registered width, known vanilla metrics, or five pixels otherwise.
     */
    public static int advance(final @Nullable Key font, final int codepoint, final boolean bold) {
        final Key key = font == null ? DEFAULT_FONT : font;
        final Map<Integer, Integer> overrides = CUSTOM_ADVANCES.get(key);
        final Integer registered = overrides == null ? null : overrides.get(codepoint);
        final int vanilla = VanillaFontMetrics.value(key, codepoint, 2);
        final int base = registered != null ? registered : vanilla >= 0 ? vanilla : UNKNOWN_ADVANCE;
        return bold ? base + 1 : base;
    }

    /**
     * Native unihex bold advances by half a pixel; the compiler pads it to the integer metric.
     */
    public static int boldHalfPixelPadding(final @Nullable Key font, final String text) {
        final Key key = font == null ? DEFAULT_FONT : font;
        return (int) text.codePoints().filter(cp -> VanillaFontMetrics.value(key, cp, 3) == 1).count();
    }

    /**
     * Whether this run needs the native font's baseline instead of an ASCII offset font.
     */
    public static boolean nativeBaseline(final @Nullable Key font, final String text) {
        return (font != null && !font.equals(DEFAULT_FONT)) || text.codePoints().anyMatch(cp -> !isSupported(cp));
    }

    public static int width(final String text, final boolean bold) {
        return width(null, text, bold);
    }

    public static int width(final @Nullable Key font, final String text, final boolean bold) {
        return ArabicShaper.shape(text).codePoints().map(codepoint -> advance(font, codepoint, bold)).sum();
    }

    /**
     * Longest prefix of {@code text} that fits in {@code maxWidth}.
     */
    public static String fit(final String text, final boolean bold, final int maxWidth) {
        return fit(null, text, bold, maxWidth);
    }

    public static String fit(final @Nullable Key font, final String text, final boolean bold, final int maxWidth) {
        int width = 0;
        for (int i = 0; i < text.length(); ) {
            final int codepoint = text.codePointAt(i);
            width += advance(font, codepoint, bold);
            if (width > maxWidth) {
                return text.substring(0, i);
            }
            i += Character.charCount(codepoint);
        }
        return text;
    }
}
