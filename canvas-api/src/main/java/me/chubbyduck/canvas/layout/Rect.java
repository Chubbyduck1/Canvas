package me.chubbyduck.canvas.layout;

import org.jspecify.annotations.NullMarked;

/**
 * An axis-aligned rectangle in canvas pixels.
 */
@NullMarked
public record Rect(int x, int y, int width, int height) {

    public static final Rect EMPTY = new Rect(0, 0, 0, 0);

    public Rect {
        width = Math.max(0, width);
        height = Math.max(0, height);
    }

    public static Rect of(final int x, final int y, final int width, final int height) {
        return new Rect(x, y, width, height);
    }

    public int right() {
        return this.x + this.width;
    }

    public int bottom() {
        return this.y + this.height;
    }

    public boolean isEmpty() {
        return this.width == 0 || this.height == 0;
    }

    public Rect translate(final int deltaX, final int deltaY) {
        return new Rect(this.x + deltaX, this.y + deltaY, this.width, this.height);
    }

    public Rect inset(final Insets insets) {
        return new Rect(this.x + insets.left(), this.y + insets.top(), this.width - insets.horizontal(), this.height - insets.vertical());
    }

    public Rect intersect(final Rect other) {
        final int left = Math.max(this.x, other.x);
        final int top = Math.max(this.y, other.y);
        final int right = Math.min(this.right(), other.right());
        final int bottom = Math.min(this.bottom(), other.bottom());
        return new Rect(left, top, right - left, bottom - top);
    }

    public boolean contains(final int pointX, final int pointY) {
        return pointX >= this.x && pointX < this.right() && pointY >= this.y && pointY < this.bottom();
    }

    public Size size() {
        return new Size(this.width, this.height);
    }

}
