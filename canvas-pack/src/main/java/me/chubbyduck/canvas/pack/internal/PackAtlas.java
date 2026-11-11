package me.chubbyduck.canvas.pack.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.chubbyduck.canvas.core.atlas.GlyphAtlas;
import me.chubbyduck.canvas.layout.Insets;
import me.chubbyduck.canvas.layout.Size;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The glyph lookups produced by the pack builder.
 */
@NullMarked
public class PackAtlas implements GlyphAtlas {

    public static final String NAMESPACE = "canvas";
    /**
     * Largest power-of-two space glyph; moves are built from 1, 2, 4, up to this.
     */
    public static final int MAX_SPACE_BIT = 10;
    public static final int SPACE_BASE = 0xE000;
    public static final int HALF_PIXEL_SPACE = SPACE_BASE + 2 * (MAX_SPACE_BIT + 1);
    public static final int MID_PIECES = 8;

    public final Map<String, Map<Key, SpriteData>> sprites = new HashMap<>();
    public final Map<String, Map<Key, NineData>> nines = new HashMap<>();
    public final Glyph[] bars = new Glyph[BAR_PIECES];
    private final Key[] spriteFonts = new Key[MAX_DY + 1];
    private final Key[] textFonts = new Key[MAX_DY + 1];

    public PackAtlas() {
        for (int dy = 0; dy <= MAX_DY; dy++) {
            this.spriteFonts[dy] = Key.key(NAMESPACE, "r" + dy);
            this.textFonts[dy] = Key.key(NAMESPACE, "t" + dy);
        }
    }

    /**
     * Codepoint of the space glyph moving by {@code 2^bit} pixels, forwards or backwards.
     */
    public static int spaceCodepoint(final int bit, final boolean negative) {
        return SPACE_BASE + (negative ? MAX_SPACE_BIT + 1 : 0) + bit;
    }

    /**
     * Which source rows make up rows {@code [start, end)} of a nine-slice drawn {@code height} tall.
     */
    public static List<Integer> composition(
        final int height,
        final int start,
        final int end,
        final int top,
        final int bottom,
        final int sourceHeight
    ) {
        final List<Integer> rows = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            final int fromBottom = height - i;
            final int row;
            if (fromBottom <= bottom) {
                row = sourceHeight - fromBottom;
            } else {
                row = Math.min(i, top);
            }
            rows.add(row);
        }
        return List.copyOf(rows);
    }

    private SpriteData sprite(final Key key, final String themeId) {
        final Map<Key, SpriteData> theme = this.sprites.get(themeId);
        final SpriteData data = theme == null ? null : theme.get(key);
        if (data == null) {
            throw new IllegalArgumentException("Unknown sprite " + key.asString() + " in theme " + themeId
                + ". Is its node class registered and the theme registered at bootstrap?");
        }
        return data;
    }

    private NineData nine(final Key key, final String themeId) {
        final Map<Key, NineData> theme = this.nines.get(themeId);
        final NineData data = theme == null ? null : theme.get(key);
        if (data == null) {
            throw new IllegalArgumentException("Unknown skin " + key.asString() + " in theme " + themeId
                + ". Is its node class registered and the theme registered at bootstrap?");
        }
        return data;
    }

    @Override
    public Size spriteSize(final Key sprite, final String themeId) {
        return this.sprite(sprite, themeId).size();
    }

    @Override
    public int spriteBandCount(final Key sprite, final String themeId, final int phase) {
        return this.sprite(sprite, themeId).bands()[phase].length;
    }

    @Override
    public @Nullable Glyph spriteBand(final Key sprite, final String themeId, final int phase, final int band) {
        return this.sprite(sprite, themeId).bands()[phase][band];
    }

    @Override
    public Insets nineBorder(final Key skin, final String themeId) {
        return this.nine(skin, themeId).border;
    }

    @Override
    public NineBand nineBand(final Key skin, final String themeId, final int height, final int start, final int end) {
        final NineData data = this.nine(skin, themeId);
        final List<Integer> rows = composition(height, start, end, data.border.top(), data.border.bottom(), data.sourceHeight);
        final NineBand band = data.bands.get(rows);
        if (band == null) {
            throw new IllegalStateException("No generated band for " + skin.asString() + " rows " + rows);
        }
        return band;
    }

    @Override
    public Key spriteFont(final int dy) {
        return this.spriteFonts[dy];
    }

    @Override
    public Glyph bar(final int bit) {
        return this.bars[bit];
    }

    @Override
    public Key textFont(final int dy) {
        return this.textFonts[dy];
    }

    @Override
    public String space(final int pixels) {
        if (pixels == 0) {
            return "";
        }
        final boolean negative = pixels < 0;
        int remaining = Math.abs(pixels);
        final StringBuilder out = new StringBuilder();
        final int max = 1 << MAX_SPACE_BIT;
        while (remaining >= max * 2) {
            out.appendCodePoint(spaceCodepoint(MAX_SPACE_BIT, negative));
            remaining -= max;
        }
        for (int bit = MAX_SPACE_BIT; bit >= 0; bit--) {
            if ((remaining & (1 << bit)) != 0) {
                out.appendCodePoint(spaceCodepoint(bit, negative));
            }
        }
        return out.toString();
    }

    @Override
    public String halfPixelSpace(final int count) {
        return Character.toString(HALF_PIXEL_SPACE).repeat(count);
    }

    /**
     * A sprite's size and its band glyphs for each phase.
     */
    @SuppressWarnings("ArrayRecordComponent") // never compared or hashed
    public record SpriteData(Size size, @Nullable Glyph[][] bands) {
    }

    /**
     * A nine-slice skin's borders and its band pieces per row composition.
     */
    public static class NineData {

        public final Insets border;
        public final int sourceHeight;
        public final Map<List<Integer>, NineBand> bands = new HashMap<>();

        public NineData(final Insets border, final int sourceHeight) {
            this.border = border;
            this.sourceHeight = sourceHeight;
        }
    }
}
