package me.chubbyduck.canvas.layout;

import org.jspecify.annotations.NullMarked;

/** Cross-axis alignment (or alignment within a stack). */
@NullMarked
public enum Align {

    START,
    CENTER,
    END,
    STRETCH;

    /** Get the offset of a given size inside the given space. */
    public int offset(final int space, final int size) {
        return switch (this) {
            case START, STRETCH -> 0;
            case CENTER -> (space - size) / 2;
            case END -> space - size;
        };
    }

}
