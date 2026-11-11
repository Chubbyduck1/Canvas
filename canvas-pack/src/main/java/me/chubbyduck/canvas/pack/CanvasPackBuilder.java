package me.chubbyduck.canvas.pack;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import me.chubbyduck.canvas.asset.NineSliceImage;
import me.chubbyduck.canvas.asset.NineSlicePainter;
import me.chubbyduck.canvas.asset.Painter;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.core.atlas.GlyphAtlas;
import me.chubbyduck.canvas.layout.Insets;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeInternals;
import me.chubbyduck.canvas.pack.internal.GlyphTable;
import me.chubbyduck.canvas.pack.internal.ImageTools;
import me.chubbyduck.canvas.pack.internal.PackAssetRegistrar;
import me.chubbyduck.canvas.pack.internal.PackAtlas;
import me.chubbyduck.canvas.pack.internal.PackWriter;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Builds the Canvas resource pack from the registered node classes and themes. */
@NullMarked
public class CanvasPackBuilder {

    private final List<Theme> themes = new ArrayList<>();
    private final Set<Class<? extends Node>> classes = new LinkedHashSet<>();
    private PackOptions options = PackOptions.DEFAULT;

    private static BufferedImage paint(final Key key, final Theme theme, final Painter painter) {
        try {
            return painter.paint(theme);
        } catch (final RuntimeException exception) {
            throw new IllegalStateException("Painter for " + key.asString() + " failed in theme " + theme.id(), exception);
        }
    }

    private static NineSliceImage paintNine(final String key, final Theme theme, final NineSlicePainter painter) {
        try {
            return painter.paint(theme);
        } catch (final RuntimeException exception) {
            throw new IllegalStateException("Painter for " + key + " failed in theme " + theme.id(), exception);
        }
    }

    /** Splits a sprite into row bands for each of the 9 possible vertical phases. */
    static PackAtlas.SpriteData spriteData(final BufferedImage image, final GlyphTable glyphs) {
        final int row = GlyphAtlas.ROW;
        final int height = image.getHeight();
        final GlyphAtlas.@Nullable Glyph[][] bands = new GlyphAtlas.@Nullable Glyph[row][];
        for (int phase = 0; phase < row; phase++) {
            final int count = Math.ceilDiv(height + phase, row);
            bands[phase] = new GlyphAtlas.@Nullable Glyph[count];
            for (int band = 0; band < count; band++) {
                final int top = band == 0 ? 0 : band * row - phase;
                final int bottom = Math.min(height, (band + 1) * row - phase);
                if (bottom > top) {
                    bands[phase][band] = glyphs.add(ImageTools.crop(image, 0, top, image.getWidth(), bottom - top));
                }
            }
        }
        return new PackAtlas.SpriteData(new Size(image.getWidth(), height), bands);
    }

    /**
     * Pre-generates every band a nine-slice can need: all row windows up to one row tall, for every height up to {@code
     * top + bottom + 2 rows}.
     */
    static PackAtlas.NineData nineData(final NineSliceImage nine, final GlyphTable glyphs) {
        final BufferedImage source = ImageTools.argb(nine.image());
        final Insets border = nine.border();
        final PackAtlas.NineData data = new PackAtlas.NineData(border, source.getHeight());
        final int row = GlyphAtlas.ROW;
        final int maxHeight = border.top() + border.bottom() + 2 * row;
        for (int height = 1; height <= maxHeight; height++) {
            for (int start = 0; start < height; start++) {
                for (int end = start + 1; end <= Math.min(height, start + row); end++) {
                    final List<Integer> rows = PackAtlas.composition(
                        height, start, end, border.top(), border.bottom(), source.getHeight());
                    if (!data.bands.containsKey(rows)) {
                        data.bands.put(rows, band(source, border, rows, glyphs));
                    }
                }
            }
        }
        return data;
    }

    private static GlyphAtlas.NineBand band(
        final BufferedImage source,
        final Insets border,
        final List<Integer> rows,
        final GlyphTable glyphs
    ) {
        final BufferedImage strip = new BufferedImage(source.getWidth(), rows.size(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < rows.size(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                strip.setRGB(x, y, source.getRGB(x, rows.get(y)));
            }
        }
        final GlyphAtlas.Glyph left = border.left() > 0
            ? glyphs.add(ImageTools.crop(strip, 0, 0, border.left(), rows.size()))
            : null;
        final GlyphAtlas.Glyph right = border.right() > 0
            ? glyphs.add(ImageTools.crop(strip, source.getWidth() - border.right(), 0, border.right(), rows.size()))
            : null;
        final GlyphAtlas.@Nullable Glyph[] mids = new GlyphAtlas.@Nullable Glyph[PackAtlas.MID_PIECES];
        for (int bit = 0; bit < mids.length; bit++) {
            final int width = 1 << bit;
            final BufferedImage mid = new BufferedImage(width, rows.size(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < rows.size(); y++) {
                final int color = strip.getRGB(border.left(), y);
                for (int x = 0; x < width; x++) {
                    mid.setRGB(x, y, color);
                }
            }
            mids[bit] = glyphs.add(mid);
        }
        return new GlyphAtlas.NineBand(left, border.left(), right, border.right(), mids);
    }

    public CanvasPackBuilder registerTheme(final Theme... themes) {
        for (final Theme theme : themes) {
            if (this.themes.stream().anyMatch(registered -> registered.id().equals(theme.id()))) {
                throw new IllegalArgumentException("Theme " + theme.id() + " registered twice");
            }
            this.themes.add(theme);
        }
        return this;
    }

    public CanvasPackBuilder register(final Class<? extends Node> type) {
        this.classes.add(type);
        return this;
    }

    public CanvasPackBuilder options(final PackOptions options) {
        this.options = options;
        return this;
    }

    public CanvasPack build() {
        if (this.themes.isEmpty()) {
            throw new IllegalStateException("Register at least one theme");
        }
        final Map<Key, Painter> sprites = new TreeMap<>(Comparator.comparing(Key::asString));
        final Map<Key, NineSlicePainter> skins = new TreeMap<>(Comparator.comparing(Key::asString));
        final Map<Key, Class<?>> owners = new HashMap<>();
        final Set<Class<?>> registered = new HashSet<>();

        for (final Class<? extends Node> type : this.classes) {
            registered.add(type);
            if (NodeInternals.hasDefaultRegister(type)) {
                continue;
            }
            final Node instance = NodeInternals.instantiate(type);
            NodeInternals.register(instance, new PackAssetRegistrar(type, sprites, skins, owners));
        }

        final GlyphTable glyphs = new GlyphTable();
        final PackAtlas atlas = new PackAtlas();
        for (int bit = 0; bit < GlyphAtlas.BAR_PIECES; bit++) {
            final BufferedImage bar = new BufferedImage(1 << bit, 1, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < bar.getWidth(); x++) {
                bar.setRGB(x, 0, 0xFFFFFFFF);
            }
            atlas.bars[bit] = Objects.requireNonNull(glyphs.add(bar));
        }
        for (final Theme theme : this.themes) {
            final Map<Key, PackAtlas.SpriteData> themeSprites = new HashMap<>();
            for (final Map.Entry<Key, Painter> sprite : sprites.entrySet()) {
                final BufferedImage image = ImageTools.argb(paint(sprite.getKey(), theme, sprite.getValue()));
                themeSprites.put(sprite.getKey(), spriteData(image, glyphs));
            }
            atlas.sprites.put(theme.id(), themeSprites);

            final Map<Key, PackAtlas.NineData> themeNines = new HashMap<>();
            for (final Map.Entry<Key, NineSlicePainter> skin : skins.entrySet()) {
                final NineSliceImage nine = paintNine(skin.getKey().asString(), theme, skin.getValue());
                themeNines.put(skin.getKey(), nineData(nine, glyphs));
            }
            atlas.nines.put(theme.id(), themeNines);
        }

        final Map<String, byte[]> files = PackWriter.write(this.options, glyphs.sheets());
        return new CanvasPack(atlas, files, Set.copyOf(registered));
    }
}
