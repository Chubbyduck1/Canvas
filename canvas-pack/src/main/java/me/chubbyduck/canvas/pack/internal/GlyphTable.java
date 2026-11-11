package me.chubbyduck.canvas.pack.internal;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import me.chubbyduck.canvas.core.atlas.GlyphAtlas;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Every generated glyph image, de-duplicated by content, with its codepoint.
 */
@NullMarked
public class GlyphTable {

    /**
     * First codepoint used for glyphs; U+E000..U+E0FF are reserved for space glyphs.
     */
    public static final int FIRST_GLYPH = 0xE100;
    private static final int BMP_PUA_END = 0xF8FF;
    private static final int PLANE15_START = 0xF0000;
    private static final int PLANE15_END = 0xFFFFD;

    private final Map<ImageTools.Key, GlyphAtlas.Glyph> byContent = new HashMap<>();
    private final Map<Integer, BufferedImage> images = new HashMap<>();
    private final TreeMap<Integer, List<Integer>> byWidth = new TreeMap<>();
    private int next = FIRST_GLYPH;

    /**
     * Adds a band image (at most one row tall).
     */
    public GlyphAtlas.@Nullable Glyph add(final BufferedImage band) {
        if (band.getHeight() > GlyphAtlas.ROW) {
            throw new IllegalArgumentException("Band taller than a row: " + band.getHeight());
        }
        final int opaque = ImageTools.opaqueWidth(band);
        if (opaque == 0) {
            return null;
        }
        final BufferedImage cell = ImageTools.crop(band, 0, 0, band.getWidth(), GlyphAtlas.ROW);
        final ImageTools.Key key = ImageTools.Key.of(cell);
        final GlyphAtlas.Glyph existing = this.byContent.get(key);
        if (existing != null) {
            return existing;
        }
        final int codepoint = this.allocate();
        // Minecraft: advance = rightmost opaque column + 1, plus 1 pixel of spacing.
        final GlyphAtlas.Glyph glyph = new GlyphAtlas.Glyph(codepoint, opaque + 1);
        this.byContent.put(key, glyph);
        this.images.put(codepoint, cell);
        this.byWidth.computeIfAbsent(cell.getWidth(), unused -> new ArrayList<>()).add(codepoint);
        return glyph;
    }

    private int allocate() {
        int codepoint = this.next++;
        if (codepoint > BMP_PUA_END && codepoint < PLANE15_START) {
            codepoint = PLANE15_START;
            this.next = codepoint + 1;
        }
        if (codepoint > PLANE15_END) {
            throw new IllegalStateException("Out of private-use codepoints: too many glyphs");
        }
        return codepoint;
    }

    /**
     * Lays the glyphs out in sheets.
     */
    public List<Sheet> sheets() {
        final List<Sheet> sheets = new ArrayList<>();
        for (final Map.Entry<Integer, List<Integer>> entry : this.byWidth.entrySet()) {
            final int cellWidth = entry.getKey();
            final int columns = Math.clamp(1024 / cellWidth, 1, 16);
            final int perSheet = columns * 16;
            final List<Integer> codepoints = entry.getValue();
            for (int start = 0; start < codepoints.size(); start += perSheet) {
                final List<Integer> chunk = codepoints.subList(start, Math.min(codepoints.size(), start + perSheet));
                sheets.add(this.sheet(sheets.size(), cellWidth, columns, chunk));
            }
        }
        return sheets;
    }

    private Sheet sheet(final int index, final int cellWidth, final int columns, final List<Integer> codepoints) {
        final int rows = Math.ceilDiv(codepoints.size(), columns);
        final BufferedImage image = new BufferedImage(
            cellWidth * columns, GlyphAtlas.ROW * rows, BufferedImage.TYPE_INT_ARGB);
        final List<String> chars = new ArrayList<>(rows);
        for (int row = 0; row < rows; row++) {
            final StringBuilder line = new StringBuilder();
            for (int column = 0; column < columns; column++) {
                final int indexInSheet = row * columns + column;
                if (indexInSheet >= codepoints.size()) {
                    line.append('\u0000');
                    continue;
                }
                final int codepoint = codepoints.get(indexInSheet);
                line.appendCodePoint(codepoint);
                image.getGraphics().drawImage(this.images.get(codepoint), column * cellWidth, row * GlyphAtlas.ROW, null);
            }
            chars.add(line.toString());
        }
        return new Sheet("font/sheet_" + index + ".png", image, chars);
    }

    /**
     * A texture holding a grid of glyphs.
     */
    public record Sheet(String path, BufferedImage image, List<String> chars) {
    }
}
