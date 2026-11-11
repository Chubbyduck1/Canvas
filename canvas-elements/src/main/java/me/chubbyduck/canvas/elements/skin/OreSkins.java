package me.chubbyduck.canvas.elements.skin;

import me.chubbyduck.canvas.asset.NineSliceImage;
import me.chubbyduck.canvas.asset.NineSlicePainter;
import me.chubbyduck.canvas.asset.Painter;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.layout.Insets;
import org.jspecify.annotations.NullMarked;

/**
 * OreUI-style painters, all generated from theme tokens.
 */
@NullMarked
public final class OreSkins {

    /**
     * Button source size: outline, frame, face, frame, lip, lip, outline (rows) by outline, frame, face, frame, outline
     * (columns).
     */
    private static final Insets BUTTON_BORDER = Insets.of(2, 2, 4, 2);

    private OreSkins() {
    }

    /**
     * A raised button.
     */
    public static NineSlicePainter button(final Variant variant) {
        return theme -> {
            final Theme.Palette palette = theme.palette();
            final PixelCanvas pixels = PixelCanvas.canvas(5, 7);
            switch (variant) {
                case SELECTED -> pixels.fill(palette.accent())
                    .fill(1, 1, 3, 1, palette.accentHighlight())
                    .fill(1, 1, 1, 3, palette.accentHighlight())
                    .fill(3, 1, 1, 3, palette.accentEdge())
                    .fill(1, 3, 3, 1, palette.accentEdge())
                    .fill(1, 4, 3, 2, palette.accentShade())
                    .outline(palette.outline());
                case DISABLED -> pixels.fill(palette.disabled())
                    .fill(1, 4, 3, 2, OreSkins.darken(palette.disabled(), 0.2f))
                    .outline(palette.outline());
                default -> {
                    final int face = palette.surfaceRaised();
                    pixels.fill(face)
                        .outline(1, 1, 3, 3, palette.highlight())
                        .set(3, 1, palette.highlightStrong())
                        .set(1, 3, palette.highlightStrong())
                        .fill(1, 4, 3, 2, palette.shade())
                        .outline(palette.outlineStrong());
                }
            }
            return new NineSliceImage(pixels.image(), BUTTON_BORDER);
        };
    }

    /**
     * Vertical offset, from a button's top, at which to draw 8px-tall content so it is centered on the face (inside the
     * outline, above the 2px lip).
     */
    public static int buttonContentY(final int height, final int contentHeight) {
        final int faceTop = 1;
        final int faceHeight = height - 1 - 3;
        return faceTop + Math.max(0, faceHeight - contentHeight) / 2;
    }

    /**
     * A card body: outline, light top/left edge, darker right edge, rule line and 2px lip at the bottom.
     */
    public static NineSlicePainter panel() {
        return theme -> {
            final Theme.Palette palette = theme.palette();
            final PixelCanvas pixels = PixelCanvas.canvas(5, 7)
                .fill(palette.surface())
                .fill(1, 1, 3, 1, palette.highlightStrong())
                .fill(1, 1, 1, 3, palette.highlightStrong())
                .fill(3, 1, 1, 3, palette.edgeShade())
                .fill(1, 3, 3, 1, palette.rule())
                .fill(1, 4, 3, 2, palette.shade())
                .outline(palette.outline());
            return new NineSliceImage(pixels.image(), Insets.of(2, 2, 4, 2));
        };
    }

    /**
     * A card header strip, drawn over the top of the card body: outline, light top/left edge, header fill, darker right
     * edge and a two-tone rule underneath.
     */
    public static NineSlicePainter panelHeader() {
        return theme -> {
            final Theme.Palette palette = theme.palette();
            final PixelCanvas pixels = PixelCanvas.canvas(5, 5)
                .fill(palette.header())
                .fill(1, 1, 3, 1, palette.highlightStrong())
                .fill(1, 1, 1, 2, palette.highlightStrong())
                .fill(3, 1, 1, 2, palette.edgeShade())
                .fill(1, 3, 3, 1, palette.edgeShade())
                .fill(1, 4, 3, 1, palette.rule())
                .fill(0, 0, 5, 1, palette.outline())
                .fill(0, 0, 1, 5, palette.outline())
                .fill(4, 0, 1, 5, palette.outline());
            return new NineSliceImage(pixels.image(), Insets.of(2, 2, 2, 2));
        };
    }

    /**
     * A sunken well: dark inner shadow top/left.
     */
    public static NineSlicePainter sunken() {
        return theme -> {
            final Theme.Palette palette = theme.palette();
            final PixelCanvas pixels = PixelCanvas.canvas(5, 5)
                .fill(palette.surfaceSunken())
                .fill(1, 1, 3, 1, darken(palette.surfaceSunken(), 0.3f))
                .fill(1, 1, 1, 3, darken(palette.surfaceSunken(), 0.3f))
                .outline(palette.outline());
            return new NineSliceImage(pixels.image(), Insets.of(2, 2, 2, 2));
        };
    }

    /**
     * A sunken well filled with the accent color (the filled part of a slider track).
     */
    public static NineSlicePainter sunkenAccent() {
        return theme -> {
            final Theme.Palette palette = theme.palette();
            final PixelCanvas pixels = PixelCanvas.canvas(5, 5)
                .fill(palette.accent())
                .fill(1, 1, 3, 1, palette.accentShade())
                .fill(1, 1, 1, 3, palette.accentShade())
                .outline(palette.outline());
            return new NineSliceImage(pixels.image(), Insets.of(2, 2, 2, 2));
        };
    }

    /**
     * A one-pixel rule in the card rule color.
     */
    public static NineSlicePainter rule() {
        return theme -> new NineSliceImage(
            PixelCanvas.canvas(3, 3).fill(theme.palette().rule()).image(),
            Insets.of(1, 1, 1, 1)
        );
    }

    /**
     * Slider thumb, 6x14: a small button.
     */
    public static Painter sliderThumb() {
        return theme -> {
            final Theme.Palette palette = theme.palette();
            return PixelCanvas.canvas(6, 14)
                .fill(palette.surfaceRaised())
                .outline(1, 1, 4, 10, palette.highlight())
                .set(4, 1, palette.highlightStrong())
                .set(1, 10, palette.highlightStrong())
                .fill(1, 11, 4, 2, palette.shade())
                .outline(palette.outlineStrong())
                .image();
        };
    }

    /**
     * Toggle switch, 28x14: a sunken (off) or accent (on) track with a button-styled knob.
     */
    public static Painter toggle(final boolean on) {
        return theme -> {
            final Theme.Palette palette = theme.palette();
            final PixelCanvas pixels = PixelCanvas.canvas(28, 14);
            if (on) {
                pixels.fill(palette.accent())
                    .fill(1, 1, 26, 1, palette.accentShade())
                    .outline(palette.outline());
            } else {
                pixels.fill(palette.surfaceSunken())
                    .fill(1, 1, 26, 1, darken(palette.surfaceSunken(), 0.3f))
                    .outline(palette.outline());
            }
            final int knobX = on ? 14 : 0;
            pixels.fill(knobX, 0, 14, 14, palette.surfaceRaised())
                .outline(knobX + 1, 1, 12, 10, palette.highlight())
                .set(knobX + 12, 1, palette.highlightStrong())
                .set(knobX + 1, 10, palette.highlightStrong())
                .fill(knobX + 1, 11, 12, 2, palette.shade())
                .outline(knobX, 0, 14, 14, palette.outlineStrong());
            // OreUI's knob mark: a bar when on, a hollow square when off.
            if (on) {
                pixels.fill(knobX + 6, 3, 2, 6, palette.accent());
            } else {
                pixels.outline(knobX + 5, 3, 4, 6, palette.shade());
            }
            return pixels.image();
        };
    }

    /**
     * A pixel arrow, 7x7.
     */
    public static Painter arrow(final boolean left) {
        return theme -> {
            final int color = theme.palette().text();
            final String[] right = {
                "...#...",
                "....#..",
                ".....#.",
                "#######",
                ".....#.",
                "....#..",
                "...#...",
            };
            final String[] rows = new String[right.length];
            for (int i = 0; i < right.length; i++) {
                rows[i] = left ? new StringBuilder(right[i]).reverse().toString() : right[i];
            }
            return PixelCanvas.canvas(7, 7).shape(0, 0, color, rows).image();
        };
    }

    public static int lighten(final int argb, final float amount) {
        return mix(argb, 0xFFFFFFFF, amount);
    }

    public static int darken(final int argb, final float amount) {
        return mix(argb, 0xFF000000, amount);
    }

    private static int mix(final int from, final int to, final float amount) {
        final int alpha = from >>> 24;
        final int red = Math.round(((from >> 16) & 0xFF) * (1 - amount) + ((to >> 16) & 0xFF) * amount);
        final int green = Math.round(((from >> 8) & 0xFF) * (1 - amount) + ((to >> 8) & 0xFF) * amount);
        final int blue = Math.round((from & 0xFF) * (1 - amount) + (to & 0xFF) * amount);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
