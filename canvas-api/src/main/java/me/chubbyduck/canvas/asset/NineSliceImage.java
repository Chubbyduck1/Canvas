package me.chubbyduck.canvas.asset;

import java.awt.image.BufferedImage;
import me.chubbyduck.canvas.layout.Insets;
import org.jspecify.annotations.NullMarked;

/**
 * A nine-slice source: the image and the size of its fixed borders.
 */
@NullMarked
public record NineSliceImage(BufferedImage image, Insets border) {

    public NineSliceImage {
        if (border.horizontal() >= image.getWidth() || border.vertical() >= image.getHeight()) {
            throw new IllegalArgumentException("Nine-slice borders must leave at least one middle row and column");
        }
    }

}
