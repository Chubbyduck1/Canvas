package me.chubbyduck.canvas.layout;

import org.jspecify.annotations.NullMarked;

/**
 * A width and height in canvas pixels.
 */
@NullMarked
public record Size(int width, int height) {

    public static final Size ZERO = new Size(0, 0);

    public Size {
        width = Math.max(0, width);
        height = Math.max(0, height);
    }

    public static Size of(final int width, final int height) {
        return new Size(width, height);
    }

    public Size grow(final Insets insets) {
        return new Size(this.width + insets.horizontal(), this.height + insets.vertical());
    }

    public int along(final Direction direction) {
        return direction == Direction.ROW ? this.width : this.height;
    }

    public int across(final Direction direction) {
        return direction == Direction.ROW ? this.height : this.width;
    }

}
