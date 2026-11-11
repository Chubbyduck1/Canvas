package me.chubbyduck.canvas.asset;

import org.jspecify.annotations.NullMarked;

/**
 * Design tokens that painters turn into art.
 */
@NullMarked
public record Theme(String id, Palette palette, Metrics metrics) {

    /**
     * Colors as ARGB ints.
     */
    public record Palette(int surface, int surfaceRaised, int surfaceSunken, int header, int outline, int outlineStrong,
                          int highlight, int highlightStrong, int edgeShade, int rule, int shade, int accent,
                          int accentHighlight, int accentEdge, int accentShade, int text, int textMuted,
                          int textOnAccent, int disabled, int textDisabled) {
    }

    /**
     * Sizes in pixels.
     */
    public record Metrics(int outline, int bevel, int buttonHeight, int headerHeight, int padding) {
    }

}
