package me.chubbyduck.canvas.core.atlas;

import me.chubbyduck.canvas.layout.Insets;
import me.chubbyduck.canvas.layout.Size;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Lookup of generated glyphs, implemented by the pack builder.
 */
@NullMarked
public interface GlyphAtlas {

    /**
     * Height of one text row, in pixels.
     */
    int ROW = 9;

    /**
     * Largest vertical offset a font exists for.
     */
    int MAX_DY = 26;
    /**
     * Number of bar pieces: widths {@code 1, 2, 4 .. 2^(BAR_PIECES-1)}.
     */
    int BAR_PIECES = 8;

    Size spriteSize(Key sprite, String themeId);

    int spriteBandCount(Key sprite, String themeId, int phase);

    @Nullable Glyph spriteBand(Key sprite, String themeId, int phase, int band);

    Insets nineBorder(Key skin, String themeId);

    /**
     * Pieces for drawing rows {@code [start, end)} of a nine-slice drawn {@code height} pixels tall.
     */
    NineBand nineBand(Key skin, String themeId, int height, int start, int end);

    Key spriteFont(int dy);

    /**
     * A solid white bar one pixel tall, drawn with the sprite fonts and tinted by the text color.
     */
    Glyph bar(int bit);

    Key textFont(int dy);

    /**
     * Explicit font keys are supplied by the caller's resource pack and retain their own baseline.
     */
    default Key textFont(final @Nullable Key font, final int dy) {
        return font == null || font.equals(TextMetrics.DEFAULT_FONT) ? textFont(dy) : font;
    }

    /**
     * A string of space glyphs that moves the cursor by exactly {@code pixels} pixels (may be negative).
     */
    String space(int px);

    /**
     * Advance of a character in the Canvas text fonts.
     */
    default int textAdvance(final int codepoint, final boolean bold) {
        return TextMetrics.advance(codepoint, bold);
    }

    /**
     * Font-aware advance used for cursor positioning.
     */
    default int textAdvance(final @Nullable Key font, final int codepoint, final boolean bold) {
        return font == null || font.equals(TextMetrics.DEFAULT_FONT)
            ? textAdvance(codepoint, bold) : TextMetrics.advance(font, codepoint, bold);
    }

    /**
     * Half-pixel padding for native unihex bold. Atlases without such fonts need no padding.
     */
    default String halfPixelSpace(final int count) {
        return "";
    }

    /**
     * A generated glyph.
     */
    record Glyph(int codepoint, int advance) {

        public String text() {
            return Character.toString(this.codepoint);
        }
    }

    /**
     * Pieces of one band of a nine-slice.
     */
    @SuppressWarnings("ArrayRecordComponent") // never compared or hashed
    record NineBand(
        @Nullable Glyph left,
        int leftWidth,
        @Nullable Glyph right,
        int rightWidth,
        @Nullable Glyph[] mids
    ) {
    }
}
