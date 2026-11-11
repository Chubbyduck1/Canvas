package me.chubbyduck.canvas.codegen.fonts;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@code "type": "bitmap"}: a texture split into an equal grid, one row of {@code chars} per grid row ({@code \0} marks an
 * empty cell).
 */
public record BitmapProvider(String file,
                             @Nullable Integer height, int ascent, List<String> chars,
                             @Nullable Map<String, Boolean> filter) implements FontProvider {

    /**
     * Bold draws bitmap glyphs a whole pixel apart, in half-pixels.
     */
    public static final int BOLD_OFFSET = 2;

    /**
     * Height glyphs are drawn at, in pixels; the client defaults it to 8.
     */
    public int heightOrDefault() {
        return this.height == null ? 8 : this.height;
    }

    /**
     * Codepoint to {@code {advance, boldOffset}}, measured from this provider's texture. Advance = occupied width scaled to
     * the provider height, rounded, plus one pixel of spacing.
     */
    @NonNull
    public Map<Integer, int[]> glyphs(final @NonNull BufferedImage image) {
        final int columns = this.chars.getFirst().codePointCount(0, this.chars.getFirst().length());
        final int glyphWidth = image.getWidth() / columns;
        final int glyphHeight = image.getHeight() / this.chars.size();
        final float scale = (float) this.heightOrDefault() / glyphHeight;
        final Map<Integer, int[]> glyphs = new HashMap<>();

        for (int row = 0; row < this.chars.size(); row++) {
            final int[] codepoints = this.chars.get(row).codePoints().toArray();

            for (int column = 0; column < codepoints.length; column++) {
                if (codepoints[column] == 0) {
                    continue;
                }

                final int width = this.occupiedWidth(image, column * glyphWidth, row * glyphHeight, glyphWidth, glyphHeight);
                glyphs.put(codepoints[column], new int[]{(int) (0.5F + width * scale) + 1, BOLD_OFFSET});
            }
        }

        return glyphs;
    }

    /**
     * Rightmost non-transparent column of a cell, plus one (0 for an empty cell).
     */
    private int occupiedWidth(final @NonNull BufferedImage image, final int x, final int y, final int width, final int height) {
        final boolean alpha = image.getColorModel().hasAlpha();
        for (int column = width - 1; column >= 0; column--) {
            for (int row = 0; row < height; row++) {
                final int argb = image.getRGB(x + column, y + row);
                if ((alpha ? argb >>> 24 : argb & 0xFFFFFF) != 0) {
                    return column + 1;
                }
            }
        }
        return 0;
    }

}
