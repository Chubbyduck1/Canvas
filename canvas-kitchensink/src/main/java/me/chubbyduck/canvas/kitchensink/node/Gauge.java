package me.chubbyduck.canvas.kitchensink.node;

import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.elements.skin.PixelCanvas;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

/** Example third-party primitive: a segmented meter. */
@NullMarked
public class Gauge extends Node {

    public static final int LEVELS = 8;
    private static final Sprite[] SPRITES = new Sprite[LEVELS + 1];

    private final int value;

    private Gauge() {
        this(0);
    }

    public Gauge(final int value) {
        this.value = Math.clamp(value, 0, LEVELS);
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        for (int level = 0; level <= LEVELS; level++) {
            final int filled = level;
            SPRITES[level] = assets.registerSprite(Key.key("kitchensink", "gauge/level_" + level), theme -> {
                final PixelCanvas pixels = PixelCanvas.canvas(4 + LEVELS * 6, 12)
                    .fill(theme.palette().surfaceSunken())
                    .outline(theme.palette().outline());
                for (int i = 0; i < LEVELS; i++) {
                    final int color = i < filled
                        ? (i < LEVELS - 2 ? theme.palette().accent() : 0xFFD83B3B)
                        : theme.palette().shade();
                    pixels.fill(3 + i * 6, 3, 4, 6, color);
                }
                return pixels.image();
            });
        }
    }

    @Override
    protected Size measure(final LayoutContext context) {
        return context.spriteSize(SPRITES[0]);
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        context.sprite(SPRITES[this.value], rect.x(), rect.y());
    }

    @Override
    protected void plain(final PlainContext context) {
        context.text(Component.text("[" + "#".repeat(this.value) + "-".repeat(LEVELS - this.value) + "]"));
    }
}
