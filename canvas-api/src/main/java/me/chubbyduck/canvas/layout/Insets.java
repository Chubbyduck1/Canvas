package me.chubbyduck.canvas.layout;

import org.jspecify.annotations.NullMarked;

/**
 * Padding or border widths.
 */
@NullMarked
public record Insets(int top, int right, int bottom, int left) {

    public static final Insets NONE = new Insets(0, 0, 0, 0);

    public static Insets all(final int value) {
        return new Insets(value, value, value, value);
    }

    public static Insets of(final int vertical, final int horizontal) {
        return new Insets(vertical, horizontal, vertical, horizontal);
    }

    public static Insets of(final int top, final int right, final int bottom, final int left) {
        return new Insets(top, right, bottom, left);
    }

    public int horizontal() {
        return this.left + this.right;
    }

    public int vertical() {
        return this.top + this.bottom;
    }

}
