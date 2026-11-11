package me.chubbyduck.canvas.core.atlas;

import org.jspecify.annotations.NullMarked;

/**
 * Advance widths of the vanilla default font's ASCII glyphs ({@code minecraft:font/ascii.png}), which the Canvas text
 * fonts reuse.
 */
@NullMarked
public final class TextMetrics {

    /** Character drawn in place of anything outside printable ASCII. */
    public static final char REPLACEMENT = '?';

    private static final int FIRST = 0x20;
    private static final int LAST = 0x7E;

    private static final int[] ADVANCES = {
        // space ! " # $ % & ' ( ) * + , - . /
        4, 2, 4, 6, 6, 6, 6, 2, 4, 4, 4, 6, 2, 6, 2, 6,
        // 0-9 : ; < = > ?
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6,
        // @ A-O
        7, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6,
        // P-Z [ \ ] ^ _
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6,
        // ` a-o
        3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6,
        // p-z { | } ~
        6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 4, 2, 4, 7,
    };

    private TextMetrics() {
    }

    public static boolean isSupported(final int codepoint) {
        return codepoint >= FIRST && codepoint <= LAST;
    }

    /** Replaces unsupported characters with {@link #REPLACEMENT}. */
    public static String sanitize(final String text) {
        final StringBuilder out = new StringBuilder(text.length());
        text.codePoints().forEach(codepoint -> out.append(isSupported(codepoint) ? (char) codepoint : REPLACEMENT));
        return out.toString();
    }

    public static int advance(final int codepoint, final boolean bold) {
        final int cp = isSupported(codepoint) ? codepoint : REPLACEMENT;
        final int base = ADVANCES[cp - FIRST];
        // Bold draws each glyph twice, one pixel apart, and adds a pixel to every advance (spaces
        // included, as seen in the 1.21.11 client).
        return bold ? base + 1 : base;
    }

    public static int width(final String text, final boolean bold) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += advance(text.charAt(i), bold);
        }
        return width;
    }

    /** Longest prefix of {@code text} that fits in {@code maxWidth}. */
    public static String fit(final String text, final boolean bold, final int maxWidth) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += advance(text.charAt(i), bold);
            if (width > maxWidth) {
                return text.substring(0, i);
            }
        }
        return text;
    }
}
