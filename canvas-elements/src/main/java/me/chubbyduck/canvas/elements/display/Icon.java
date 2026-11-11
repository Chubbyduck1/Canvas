package me.chubbyduck.canvas.elements.display;

import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.PaintContext;
import org.jspecify.annotations.NullMarked;

/** Draws a registered sprite at its natural size, centered in the node's rectangle. */
@NullMarked
public class Icon extends Node {

    private final Sprite sprite;

    protected Icon(final Sprite sprite) {
        this.sprite = sprite;
    }

    public static Icon of(final Sprite sprite) {
        return new Icon(sprite);
    }

    @Override
    protected Size measure(final LayoutContext context) {
        return context.spriteSize(this.sprite);
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        final Size size = context.spriteSize(this.sprite);
        context.sprite(
            this.sprite,
            rect.x() + (rect.width() - size.width()) / 2,
            rect.y() + (rect.height() - size.height()) / 2
        );
    }
}
