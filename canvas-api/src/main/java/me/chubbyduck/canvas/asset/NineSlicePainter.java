package me.chubbyduck.canvas.asset;

import org.jspecify.annotations.NullMarked;

/** Paints a nine-slice source image for a theme. */
@FunctionalInterface
@NullMarked
public interface NineSlicePainter {

    NineSliceImage paint(Theme theme);

}
