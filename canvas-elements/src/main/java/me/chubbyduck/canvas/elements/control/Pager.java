package me.chubbyduck.canvas.elements.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Column;
import me.chubbyduck.canvas.elements.layout.Composite;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.Justify;
import me.chubbyduck.canvas.layout.Length;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Shows one page at a time with ◀ n / total ▶ controls underneath. */
@NullMarked
public class Pager extends Composite {

    private final List<NodeLike> pages;
    private final ValueBinding<Integer> page;

    private Pager(final Builder builder) {
        this.pages = List.copyOf(builder.pages);
        this.page = Objects.requireNonNull(builder.page, "Pager needs page(...)");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Node build() {
        final int count = Math.max(1, this.pages.size());
        final int current = Math.clamp(this.page.get(), 0, count - 1);
        return Column.builder()
            .gap(4)
            .align(Align.STRETCH)
            .add(this.pages.isEmpty() ? Text.muted("Nothing here") : this.pages.get(current))
            .add(Row.builder()
                .width(Length.FILL)
                .justify(Justify.CENTER)
                .align(Align.CENTER)
                .gap(4)
                .add(ArrowButton.left(() -> this.page.set(current - 1)).disabled(current == 0))
                .add(Text.of((current + 1) + " / " + count))
                .add(ArrowButton.right(() -> this.page.set(current + 1)).disabled(current >= count - 1))
                .build())
            .build();
    }

    public static class Builder {

        private final List<NodeLike> pages = new ArrayList<>();
        private @Nullable ValueBinding<Integer> page;

        public Builder pages(final NodeLike... pages) {
            this.pages.addAll(Arrays.asList(pages));
            return this;
        }

        public Builder pages(final List<? extends NodeLike> pages) {
            this.pages.addAll(pages);
            return this;
        }

        public Builder page(final State<Integer> state) {
            this.page = ValueBinding.bound(state);
            return this;
        }

        public Pager build() {
            return new Pager(this);
        }
    }
}
