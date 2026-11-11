package me.chubbyduck.canvas.elements.skin;

import java.awt.image.BufferedImage;
import org.jspecify.annotations.NullMarked;

/** Pixel-art drawing primitives for painters. */
@NullMarked
public final class PixelCanvas {

    private final BufferedImage image;

    private PixelCanvas(final int width, final int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public static PixelCanvas canvas(final int width, final int height) {
        return new PixelCanvas(width, height);
    }

    public PixelCanvas fill(final int x, final int y, final int width, final int height, final int argb) {
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                this.set(x + column, y + row, argb);
            }
        }
        return this;
    }

    public PixelCanvas fill(final int argb) {
        return this.fill(0, 0, this.image.getWidth(), this.image.getHeight(), argb);
    }

    /** A one-pixel frame around the given rectangle's inside edge. */
    public PixelCanvas outline(final int x, final int y, final int width, final int height, final int argb) {
        this.fill(x, y, width, 1, argb);
        this.fill(x, y + height - 1, width, 1, argb);
        this.fill(x, y, 1, height, argb);
        this.fill(x + width - 1, y, 1, height, argb);
        return this;
    }

    public PixelCanvas outline(final int argb) {
        return this.outline(0, 0, this.image.getWidth(), this.image.getHeight(), argb);
    }

    public PixelCanvas set(final int x, final int y, final int argb) {
        if (x >= 0 && y >= 0 && x < this.image.getWidth() && y < this.image.getHeight()) {
            this.image.setRGB(x, y, argb);
        }
        return this;
    }

    /** Draws a shape from rows of characters: {@code #} is filled, anything else is transparent. */
    public PixelCanvas shape(final int x, final int y, final int argb, final String... rows) {
        for (int row = 0; row < rows.length; row++) {
            for (int column = 0; column < rows[row].length(); column++) {
                if (rows[row].charAt(column) == '#') {
                    this.set(x + column, y + row, argb);
                }
            }
        }
        return this;
    }

    public int width() {
        return this.image.getWidth();
    }

    public int height() {
        return this.image.getHeight();
    }

    public BufferedImage image() {
        return this.image;
    }
}
