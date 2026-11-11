package me.chubbyduck.canvas.asset;

import java.awt.image.BufferedImage;
import me.chubbyduck.canvas.node.Node;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

/**
 * Passed to {@link Node#register}.
 */
@NullMarked
public interface AssetRegistrar {

    /**
     * A single image, painted per theme.
     */
    Sprite registerSprite(Key key, Painter painter);

    /**
     * A fixed image that is the same in every theme.
     */
    default Sprite registerImage(Key key, BufferedImage image) {
        return this.registerSprite(key, _ -> image);
    }

    /**
     * A nine-slice skin that can be drawn at any size.
     */
    NineSlice registerNineSlice(Key key, NineSlicePainter painter);

}
