package me.chubbyduck.canvas.elements.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.Direction;
import me.chubbyduck.canvas.layout.Insets;
import me.chubbyduck.canvas.layout.Justify;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Length;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A flexbox subset: children laid out along a main axis, with grow/shrink, justify, cross-axis alignment, gaps, padding
 * and optional wrapping.
 */
@NullMarked
public class Flex extends Node {

    private final List<NodeLike> children;
    private final Direction direction;
    private final Justify justify;
    private final Align align;
    private final boolean wrap;
    private final int gap;
    private final Insets padding;

    protected Flex(final Builder builder) {
        this.children = List.copyOf(builder.children);
        this.direction = builder.direction;
        this.justify = builder.justify;
        this.align = builder.align;
        this.wrap = builder.wrap && builder.direction == Direction.ROW;
        this.gap = builder.gap;
        this.padding = builder.padding;
    }

    public static Builder builder() {
        return new Builder(Direction.ROW);
    }

    public static Builder row() {
        return new Builder(Direction.ROW);
    }

    public static Builder column() {
        return new Builder(Direction.COLUMN);
    }

    private static int minus(final int available, final int amount) {
        return available == Integer.MAX_VALUE ? available : Math.max(0, available - amount);
    }

    @Override
    public List<? extends NodeLike> children() {
        return this.children;
    }

    @Override
    protected Size measure(final LayoutContext context) {
        final List<LayoutContext.Child> children = context.children();
        final int innerMax = minus(context.maxWidth(), this.padding.horizontal());
        int width = 0;
        int height = 0;
        if (this.direction == Direction.COLUMN) {
            for (final LayoutContext.Child child : children) {
                final Size size = child.measure(innerMax);
                width = Math.max(width, size.width());
                height += size.height();
            }
            height += Math.max(0, children.size() - 1) * this.gap;
        } else {
            for (final List<LayoutContext.Child> line : this.lines(children, innerMax)) {
                int lineWidth = 0;
                int lineHeight = 0;
                for (final Size size : this.rowSizes(line, innerMax)) {
                    lineWidth += size.width();
                    lineHeight = Math.max(lineHeight, size.height());
                }
                lineWidth += Math.max(0, line.size() - 1) * this.gap;
                width = Math.max(width, lineWidth);
                height += lineHeight + (height > 0 ? this.gap : 0);
            }
        }
        return new Size(width, height).grow(this.padding);
    }

    @Override
    protected void arrange(final Rect rect, final LayoutContext context) {
        final Rect inner = rect.inset(this.padding);
        final List<LayoutContext.Child> children = context.children();
        if (children.isEmpty()) {
            return;
        }
        if (this.direction == Direction.COLUMN) {
            this.arrangeLine(children, inner, 0, inner.width());
            return;
        }
        final List<List<LayoutContext.Child>> lines = this.lines(children, inner.width());
        if (lines.size() == 1) {
            this.arrangeLine(lines.getFirst(), inner, 0, inner.height());
            return;
        }
        int top = 0;
        for (final List<LayoutContext.Child> line : lines) {
            int lineHeight = 0;
            for (final LayoutContext.Child child : line) {
                lineHeight = Math.max(lineHeight, child.measure(inner.width()).height());
            }
            this.arrangeLine(line, inner, top, lineHeight);
            top += lineHeight + this.gap;
        }
    }

    /** Sizes of a row's children for measuring. */
    private List<Size> rowSizes(final List<LayoutContext.Child> line, final int space) {
        final List<Size> sizes = new ArrayList<>(line.size());
        if (space == Integer.MAX_VALUE) {
            for (final LayoutContext.Child child : line) {
                sizes.add(child.measure(space));
            }
            return sizes;
        }
        float totalGrow = 0;
        int fixed = Math.max(0, line.size() - 1) * this.gap;
        final @Nullable Size[] measured = new @Nullable Size[line.size()];
        for (int i = 0; i < line.size(); i++) {
            final Node node = line.get(i).node();
            final float grow = node.width().isFill() ? Math.max(1, node.grow()) : node.grow();
            if (grow > 0) {
                totalGrow += grow;
                continue;
            }
            final Size size = line.get(i).measure(space);
            measured[i] = size;
            fixed += size.width();
        }
        final int remaining = Math.max(0, space - fixed);
        for (int i = 0; i < line.size(); i++) {
            final Size known = measured[i];
            if (known != null) {
                sizes.add(known);
                continue;
            }
            final Node node = line.get(i).node();
            final float grow = node.width().isFill() ? Math.max(1, node.grow()) : node.grow();
            sizes.add(line.get(i).measure((int) Math.floor(remaining * grow / totalGrow)));
        }
        return sizes;
    }

    /**
     * Splits children into lines: one line unless wrapping, in which case a line breaks before a child that would make
     * it wider than {@code space}.
     */
    private List<List<LayoutContext.Child>> lines(final List<LayoutContext.Child> children, final int space) {
        if (!this.wrap) {
            return List.of(children);
        }
        final List<List<LayoutContext.Child>> lines = new ArrayList<>();
        List<LayoutContext.Child> line = new ArrayList<>();
        int used = 0;
        for (final LayoutContext.Child child : children) {
            final int size = child.measure(space).width();
            final int needed = line.isEmpty() ? size : used + this.gap + size;
            if (!line.isEmpty() && needed > space) {
                lines.add(line);
                line = new ArrayList<>();
                used = size;
            } else {
                used = needed;
            }
            line.add(child);
        }
        if (!line.isEmpty()) {
            lines.add(line);
        }
        return lines;
    }

    private boolean stretches(final Node node, final Length crossLength) {
        final Align itemAlign = Objects.requireNonNullElse(node.alignSelf(), this.align);
        return crossLength.isFill() || (itemAlign == Align.STRETCH && !crossLength.isFixed());
    }

    /**
     * Takes {@code deficit} pixels from the children in proportion to {@code shrink * size}, never below their floors.
     */
    private int shrinkLine(final List<LayoutContext.Child> line, final int[] main, final int[] floors, final int deficit) {
        int remaining = deficit;
        while (remaining > 0) {
            float total = 0;
            int last = -1;
            for (int i = 0; i < main.length; i++) {
                if (line.get(i).node().shrink() <= 0 || main[i] <= floors[i]) {
                    continue;
                }
                total += line.get(i).node().shrink() * main[i];
                last = i;
            }
            if (last < 0 || total <= 0) {
                break;
            }
            int taken = 0;
            for (int i = 0; i < main.length; i++) {
                if (line.get(i).node().shrink() <= 0 || main[i] <= floors[i]) {
                    continue;
                }
                final int cut = Math.min(main[i] - floors[i],
                    (int) Math.floor(remaining * line.get(i).node().shrink() * main[i] / total));
                main[i] -= cut;
                taken += cut;
            }
            if (taken == 0) {
                // Rounding left less than a pixel each: take it from the last shrinkable child.
                main[last]--;
                taken = 1;
            }
            remaining -= taken;
        }
        return remaining;
    }

    private void arrangeLine(
        final List<LayoutContext.Child> line,
        final Rect inner,
        final int crossStart,
        final int lineCross
    ) {
        final boolean row = this.direction == Direction.ROW;
        final int mainSpace = row ? inner.width() : inner.height();
        final int count = line.size();
        final int[] main = new int[count];
        final int[] cross = new int[count];
        final float[] grows = new float[count];
        // The smallest each child may shrink to on the main axis.
        final int[] floors = new int[count];
        float totalGrow = 0;
        int used = (count - 1) * this.gap;
        // Growing children start from their size at their share of the free width, so wrapping
        // content (text) doesn't claim the whole row and squeeze its siblings.
        final List<Size> rowBase = row ? this.rowSizes(line, inner.width()) : List.of();

        for (int i = 0; i < count; i++) {
            final LayoutContext.Child child = line.get(i);
            final Node node = child.node();
            final Length mainLength = row ? node.width() : node.height();
            if (row) {
                main[i] = node.basis() >= 0 ? node.basis() : rowBase.get(i).width();
            } else {
                final Length crossLength = node.width();
                cross[i] = this.stretches(node, crossLength)
                    ? lineCross
                    : Math.min(lineCross, child.measure(lineCross).width());
                final int content = child.measure(cross[i]).height();
                main[i] = node.basis() >= 0 ? node.basis() : content;
                // Like CSS min-height: auto. Content doesn't get shorter when squeezed (unlike a
                // row's text, which re-wraps), and nothing clips it, so shrinking a child below
                // its content height would draw it over its siblings.
                floors[i] = mainLength.isFixed() ? node.minHeight() : Math.max(node.minHeight(), content);
            }
            if (row) {
                floors[i] = node.minWidth();
            }
            floors[i] = Math.min(floors[i], main[i]);
            grows[i] = mainLength.isFill() ? Math.max(1, node.grow()) : node.grow();
            totalGrow += grows[i];
            used += main[i];
        }

        int free = mainSpace - used;
        if (free > 0 && totalGrow > 0) {
            int given = 0;
            int lastGrowing = -1;
            for (int i = 0; i < count; i++) {
                if (grows[i] <= 0) {
                    continue;
                }
                final int extra = (int) Math.floor(free * grows[i] / totalGrow);
                main[i] += extra;
                given += extra;
                lastGrowing = i;
            }
            main[lastGrowing] += free - given;
            free = 0;
        } else if (free < 0) {
            free = -this.shrinkLine(line, main, floors, -free);
        }

        if (row) {
            for (int i = 0; i < count; i++) {
                final Node node = line.get(i).node();
                cross[i] = this.stretches(node, node.height())
                    ? lineCross
                    : line.get(i).measure(main[i]).height();
            }
        }

        int cursor = 0;
        int spacing = this.gap;
        if (free > 0) {
            switch (this.justify) {
                case CENTER -> cursor = free / 2;
                case END -> cursor = free;
                case SPACE_BETWEEN -> spacing += count > 1 ? free / (count - 1) : 0;
                case SPACE_AROUND -> {
                    final int around = free / count;
                    cursor = around / 2;
                    spacing += around;
                }
                default -> {
                }
            }
        }
        final int[] positions = new int[count];
        for (int i = 0; i < count; i++) {
            positions[i] = cursor;
            cursor += main[i] + spacing;
        }
        if (free > 0 && this.justify == Justify.SPACE_BETWEEN && count > 1) {
            // Put rounding leftovers before the last child so it ends flush with the edge.
            positions[count - 1] = mainSpace - main[count - 1];
        }

        for (int i = 0; i < count; i++) {
            final Node node = line.get(i).node();
            final Align itemAlign = Objects.requireNonNullElse(node.alignSelf(), this.align);
            final int crossOffset = crossStart + itemAlign.offset(lineCross, cross[i]);
            line.get(i).place(row
                ? new Rect(inner.x() + positions[i], inner.y() + crossOffset, main[i], cross[i])
                : new Rect(inner.x() + crossOffset, inner.y() + positions[i], cross[i], main[i]));
        }
    }

    @Override
    public String toString() {
        return "Flex" + Arrays.asList(this.direction, this.justify, this.align, this.children.size());
    }

    public static class Builder {

        private final List<NodeLike> children = new ArrayList<>();
        private Direction direction;
        private Justify justify = Justify.START;
        private Align align = Align.START;
        private boolean wrap;
        private int gap;
        private Insets padding = Insets.NONE;
        private Length width = Length.HUG;
        private Length height = Length.HUG;
        private float grow;

        protected Builder(final Direction direction) {
            this.direction = direction;
        }

        public Builder direction(final Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder justify(final Justify justify) {
            this.justify = justify;
            return this;
        }

        public Builder align(final Align align) {
            this.align = align;
            return this;
        }

        /** Lets a row break onto several lines at the available width. */
        public Builder wrap(final boolean wrap) {
            this.wrap = wrap;
            return this;
        }

        public Builder gap(final int gap) {
            this.gap = gap;
            return this;
        }

        public Builder padding(final int padding) {
            this.padding = Insets.all(padding);
            return this;
        }

        public Builder padding(final Insets padding) {
            this.padding = padding;
            return this;
        }

        public Builder width(final int width) {
            this.width = Length.px(width);
            return this;
        }

        public Builder width(final Length width) {
            this.width = width;
            return this;
        }

        public Builder height(final int height) {
            this.height = Length.px(height);
            return this;
        }

        public Builder height(final Length height) {
            this.height = height;
            return this;
        }

        /** How much of its parent flex container's free space this container takes. */
        public Builder grow(final float grow) {
            this.grow = grow;
            return this;
        }

        public Builder add(final NodeLike child) {
            this.children.add(child);
            return this;
        }

        public Builder add(final NodeLike... children) {
            this.children.addAll(Arrays.asList(children));
            return this;
        }

        public Builder addAll(final List<? extends NodeLike> children) {
            this.children.addAll(children);
            return this;
        }

        public Flex build() {
            final Flex flex = new Flex(this);
            flex.width(this.width).height(this.height).grow(this.grow);
            return flex;
        }
    }
}
