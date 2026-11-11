package me.chubbyduck.canvas.core.render;

import java.awt.image.BufferedImage;
import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.NineSlice;
import me.chubbyduck.canvas.asset.NineSliceImage;
import me.chubbyduck.canvas.layout.Insets;
import me.chubbyduck.canvas.node.Node;
import net.kyori.adventure.key.Key;

/** Textures for the debug overlay: outlines for layout boxes and translucent fills for click areas. */
public class DebugBox extends Node {

    public static NineSlice boxSkin;
    public static NineSlice hitSkin;

    private DebugBox() {
    }

    static boolean isAvailable() {
        return boxSkin != null && hitSkin != null;
    }

    private static NineSliceImage outline(final int edge, final int fill) {
        final BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                final boolean border = x != 1 || y != 1;
                image.setRGB(x, y, border ? edge : fill);
            }
        }
        return new NineSliceImage(image, Insets.all(1));
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        boxSkin = assets.registerNineSlice(Key.key("canvas", "debug_box/box"), theme -> outline(0xFFFF3B3B, 0x00000000));
        hitSkin = assets.registerNineSlice(Key.key("canvas", "debug_box/hit"), theme -> outline(0xFF2FD15A, 0x552FD15A));
    }
}
