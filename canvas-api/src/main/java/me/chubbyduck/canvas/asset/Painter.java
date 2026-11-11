package me.chubbyduck.canvas.asset;

import java.awt.image.BufferedImage;
import org.jspecify.annotations.NullMarked;

/** Paints an image for a theme. */
@FunctionalInterface
@NullMarked
public interface Painter {

    BufferedImage paint(Theme theme);

}
