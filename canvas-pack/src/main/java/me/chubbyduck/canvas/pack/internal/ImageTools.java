package me.chubbyduck.canvas.pack.internal;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Small image helpers.
 */
@NullMarked
public final class ImageTools {

    private ImageTools() {
    }

    public static BufferedImage argb(final BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source;
        }
        final BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }

    /**
     * Copies a region, filling anything outside the source with transparency.
     */
    public static BufferedImage crop(final BufferedImage source, final int x, final int y, final int width, final int height) {
        final BufferedImage output = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        for (int offsetY = 0; offsetY < height; offsetY++) {
            for (int offsetX = 0; offsetX < width; offsetX++) {
                final int sourceX = x + offsetX;
                final int sourceY = y + offsetY;
                if (sourceX >= 0 && sourceY >= 0 && sourceX < source.getWidth() && sourceY < source.getHeight()) {
                    output.setRGB(offsetX, offsetY, source.getRGB(sourceX, sourceY));
                }
            }
        }
        return output;
    }

    /**
     * Minecraft's bitmap glyph width rule: index of the rightmost column with any non-transparent pixel, plus one.
     */
    public static int opaqueWidth(final BufferedImage image) {
        for (int x = image.getWidth() - 1; x >= 0; x--) {
            for (int y = 0; y < image.getHeight(); y++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return x + 1;
                }
            }
        }
        return 0;
    }

    public static int[] pixels(final BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    public static byte[] png(final BufferedImage image) {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /**
     * Identity of an image's pixels, used to deduplicate glyphs.
     */
    @SuppressWarnings("ArrayRecordComponent") // equals and hashCode compare the pixels
    public record Key(int width, int height, int[] pixels) {

        public static Key of(final BufferedImage image) {
            return new Key(image.getWidth(), image.getHeight(), ImageTools.pixels(image));
        }

        @Override
        public boolean equals(final @Nullable Object other) {
            return other instanceof Key(final int otherWidth, final int otherHeight, final int[] otherPixels)
                && otherWidth == this.width
                && otherHeight == this.height
                && Arrays.equals(otherPixels, this.pixels);
        }

        @Override
        public int hashCode() {
            return 31 * (31 * this.width + this.height) + Arrays.hashCode(this.pixels);
        }
    }
}
