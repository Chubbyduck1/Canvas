package me.chubbyduck.canvas.elements.display;

import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import org.jspecify.annotations.NullMarked;

/**
 * Empty space: either a fixed gap, or {@link #flexible()} space that pushes flex siblings apart.
 */
@NullMarked
public class Spacer extends Node {

    private final int width;
    private final int height;

    protected Spacer(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * A spacer that takes up all free space in its flex container.
     */
    public static Spacer flexible() {
        final Spacer spacer = new Spacer(0, 0);
        spacer.grow(1);
        return spacer;
    }

    /**
     * A fixed-size gap.
     */
    public static Spacer of(final int width, final int height) {
        return new Spacer(width, height);
    }

    @Override
    protected Size measure(final LayoutContext context) {
        return new Size(this.width, this.height);
    }
}
