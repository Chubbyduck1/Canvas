package me.chubbyduck.canvas.layout;

import org.jspecify.annotations.NullMarked;

/** A size along one axis: a fixed pixel count, "fill the available space", or "hug the content". */
@NullMarked
public record Length(Kind kind, int pixels) {

    public static final Length HUG = new Length(Kind.HUG, 0);
    public static final Length FILL = new Length(Kind.FILL, 0);

    public static Length px(final int pixels) {
        return new Length(Kind.FIXED, Math.max(0, pixels));
    }

    public boolean isFixed() {
        return this.kind == Kind.FIXED;
    }

    public boolean isFill() {
        return this.kind == Kind.FILL;
    }

    /** Resolves this length against the measured content size and the available space. */
    public int resolve(final int measured, final int available) {
        return switch (this.kind) {
            case FIXED -> this.pixels;
            case FILL -> available;
            case HUG -> measured;
        };
    }

    public enum Kind {
        FIXED, FILL, HUG
    }

}
