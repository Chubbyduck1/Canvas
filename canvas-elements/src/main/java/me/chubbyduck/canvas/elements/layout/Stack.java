package me.chubbyduck.canvas.elements.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.Insets;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import org.jspecify.annotations.NullMarked;

/** Overlays children on top of each other, each aligned within the stack. */
@NullMarked
public class Stack extends Node {

    private final List<NodeLike> children;
    private final Align horizontal;
    private final Align vertical;
    private final Insets padding;

    private Stack(final Builder builder) {
        this.children = List.copyOf(builder.children);
        this.horizontal = builder.horizontal;
        this.vertical = builder.vertical;
        this.padding = builder.padding;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Stack of(final NodeLike... children) {
        return builder().add(children).build();
    }

    @Override
    public List<? extends NodeLike> children() {
        return this.children;
    }

    @Override
    protected Size measure(final LayoutContext context) {
        int width = 0;
        int height = 0;
        for (final LayoutContext.Child child : context.children()) {
            final Size measured = child.measure();
            width = Math.max(width, measured.width());
            height = Math.max(height, measured.height());
        }
        return new Size(width, height).grow(this.padding);
    }

    @Override
    protected void arrange(final Rect rect, final LayoutContext context) {
        final Rect inner = rect.inset(this.padding);
        for (final LayoutContext.Child child : context.children()) {
            final Node node = child.node();
            final Size measured = child.measure();
            final int width = node.width().isFill() || this.horizontal == Align.STRETCH ? inner.width() : measured.width();
            final int height = node.height().isFill() || this.vertical == Align.STRETCH ? inner.height() : measured.height();
            child.place(new Rect(
                inner.x() + this.horizontal.offset(inner.width(), width),
                inner.y() + this.vertical.offset(inner.height(), height),
                width,
                height
            ));
        }
    }

    public static class Builder {

        private final List<NodeLike> children = new ArrayList<>();
        private Align horizontal = Align.START;
        private Align vertical = Align.START;
        private Insets padding = Insets.NONE;

        public Builder align(final Align horizontal, final Align vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
            return this;
        }

        public Builder center() {
            return this.align(Align.CENTER, Align.CENTER);
        }

        public Builder padding(final int padding) {
            this.padding = Insets.all(padding);
            return this;
        }

        public Builder add(final NodeLike... children) {
            this.children.addAll(Arrays.asList(children));
            return this;
        }

        public Stack build() {
            return new Stack(this);
        }
    }
}
