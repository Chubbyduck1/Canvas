package me.chubbyduck.canvas.elements.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import me.chubbyduck.canvas.elements.layout.Composite;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.Direction;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A list of tab buttons with one selected: vertical like a settings sidebar, or horizontal.
 */
@NullMarked
public class Tabs<T> extends Composite {

    private final List<T> tabs;
    private final Function<? super T, ? extends Component> label;
    private final ValueBinding<T> value;
    private final boolean vertical;
    private final int buttonWidth;
    private final int gap;

    private Tabs(final Builder<T> builder) {
        this.tabs = List.copyOf(builder.tabs);
        this.label = builder.label;
        this.value = Objects.requireNonNull(builder.value, "Tabs needs value(...)");
        this.vertical = builder.vertical;
        this.buttonWidth = builder.buttonWidth;
        this.gap = builder.gap;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    protected Node build() {
        final T current = this.value.get();
        final Flex.Builder list = Flex.builder()
            .direction(this.vertical ? Direction.COLUMN : Direction.ROW)
            .gap(this.gap)
            .align(this.vertical ? Align.STRETCH : Align.START);
        for (final T tab : this.tabs) {
            list.add(Button.builder()
                .label(this.label.apply(tab))
                .selected(Objects.equals(tab, current))
                .minWidth(this.buttonWidth)
                .onClick(() -> this.value.set(tab))
                .build());
        }
        return list.build();
    }

    public static class Builder<T> {

        private final List<T> tabs = new ArrayList<>();
        private Function<? super T, ? extends Component> label = option -> Component.text(String.valueOf(option));
        private @Nullable ValueBinding<T> value;
        private boolean vertical = true;
        private int buttonWidth;
        private int gap = 2;

        @SafeVarargs
        public final Builder<T> tabs(final T... tabs) {
            this.tabs.addAll(Arrays.asList(tabs));
            return this;
        }

        public Builder<T> tabs(final List<T> tabs) {
            this.tabs.addAll(tabs);
            return this;
        }

        public Builder<T> label(final Function<? super T, String> label) {
            this.label = option -> Component.text(label.apply(option));
            return this;
        }

        /**
         * Styled labels.
         */
        public Builder<T> labelComponent(final Function<? super T, ? extends Component> label) {
            this.label = label;
            return this;
        }

        public Builder<T> value(final State<T> state) {
            this.value = ValueBinding.bound(state);
            return this;
        }

        public Builder<T> value(final T value, final Consumer<T> onChange) {
            this.value = ValueBinding.controlled(value, onChange);
            return this;
        }

        public Builder<T> vertical(final boolean vertical) {
            this.vertical = vertical;
            return this;
        }

        public Builder<T> buttonWidth(final int buttonWidth) {
            this.buttonWidth = buttonWidth;
            return this;
        }

        public Builder<T> gap(final int gap) {
            this.gap = gap;
            return this;
        }

        public Tabs<T> build() {
            return new Tabs<>(this);
        }
    }
}
