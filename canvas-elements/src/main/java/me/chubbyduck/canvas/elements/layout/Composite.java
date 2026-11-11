package me.chubbyduck.canvas.elements.layout;

import java.util.List;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A primitive built from other nodes: {@link #build()} returns a single inner node that fills this node's rectangle.
 */
@NullMarked
public abstract class Composite extends Node {

    private @Nullable Node built;

    protected abstract Node build();

    private Node inner() {
        if (this.built == null) {
            this.built = this.build();
        }
        return this.built;
    }

    @Override
    public List<? extends NodeLike> children() {
        return List.of(this.inner());
    }

    @Override
    protected Size measure(final LayoutContext context) {
        return context.children().getFirst().measure();
    }

    @Override
    protected void arrange(final Rect rect, final LayoutContext context) {
        context.children().getFirst().place(rect);
    }
}
