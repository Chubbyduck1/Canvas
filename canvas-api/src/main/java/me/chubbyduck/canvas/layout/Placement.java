package me.chubbyduck.canvas.layout;

import org.jspecify.annotations.NullMarked;

/** Where an absolutely positioned child goes. */
@NullMarked
public record Placement(int x, int y, int width, int height) {

    public static Placement at(final int x, final int y) {
        return new Placement(x, y, 0, 0);
    }

    public Placement size(final int width, final int height) {
        return new Placement(this.x, this.y, width, height);
    }

}
