package me.chubbyduck.canvas.elements.layout;

import java.util.ArrayList;
import java.util.List;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Placement;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import org.jspecify.annotations.NullMarked;

/** Places children at exact pixel positions. */
@NullMarked
public class Absolute extends Node {

    private final List<NodeLike> children;
    private final List<Entry> entries;

    private Absolute(final List<Entry> entries) {
        this.entries = List.copyOf(entries);
        this.children = this.entries.stream().map(Entry::child).toList();
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Shorthand for {@link Placement#at(int, int)}. */
    public static Placement at(final int x, final int y) {
        return Placement.at(x, y);
    }

    @Override
    public List<? extends NodeLike> children() {
        return this.children;
    }

    @Override
    protected Size measure(final LayoutContext context) {
        int width = 0;
        int height = 0;
        final List<LayoutContext.Child> children = context.children();
        for (int childIndex = 0; childIndex < children.size(); childIndex++) {
            final Size size = this.sizeOf(children.get(childIndex), this.entries.get(childIndex).placement());
            final Placement at = this.entries.get(childIndex).placement();
            width = Math.max(width, at.x() + size.width());
            height = Math.max(height, at.y() + size.height());
        }
        return new Size(width, height);
    }

    @Override
    protected void arrange(final Rect rect, final LayoutContext context) {
        final List<LayoutContext.Child> children = context.children();
        for (int childIndex = 0; childIndex < children.size(); childIndex++) {
            final Placement at = this.entries.get(childIndex).placement();
            final Size size = this.sizeOf(children.get(childIndex), at);
            children.get(childIndex).place(new Rect(rect.x() + at.x(), rect.y() + at.y(), size.width(), size.height()));
        }
    }

    private Size sizeOf(final LayoutContext.Child child, final Placement at) {
        final Size measured = child.measure(at.width() > 0 ? at.width() : Integer.MAX_VALUE);
        return new Size(
            at.width() > 0 ? at.width() : measured.width(),
            at.height() > 0 ? at.height() : measured.height()
        );
    }

    private record Entry(NodeLike child, Placement placement) {
    }

    public static class Builder {

        private final List<Entry> entries = new ArrayList<>();
        private int width;
        private int height;

        public Builder size(final int width, final int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder add(final NodeLike child, final Placement placement) {
            this.entries.add(new Entry(child, placement));
            return this;
        }

        public Absolute build() {
            final Absolute absolute = new Absolute(this.entries);
            if (this.width > 0) {
                absolute.width(this.width);
            }
            if (this.height > 0) {
                absolute.height(this.height);
            }
            return absolute;
        }
    }
}
