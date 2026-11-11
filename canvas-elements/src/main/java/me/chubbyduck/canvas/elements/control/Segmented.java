package me.chubbyduck.canvas.elements.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import me.chubbyduck.canvas.core.atlas.StyledText;
import me.chubbyduck.canvas.elements.layout.Composite;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A row of buttons where one is selected, like "On / Minimal / Off".
 */
@NullMarked
public class Segmented<T> extends Composite {

    private final List<T> options;
    private final Function<? super T, ? extends Component> label;
    private final ValueBinding<T> value;
    private final int gap;

    private Segmented(final Builder<T> builder) {
        this.options = List.copyOf(builder.options);
        this.label = builder.label;
        this.value = Objects.requireNonNull(builder.value, "Segmented needs value(...)");
        this.gap = builder.gap;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    protected Node build() {
        int width = 0;
        for (final T option : this.options) {
            width = Math.max(width, StyledText.width(this.label.apply(option)) + 16);
        }
        final T current = this.value.get();
        final Flex.Builder row = Row.builder().gap(this.gap);
        for (final T option : this.options) {
            row.add(Button.builder()
                .label(this.label.apply(option))
                .selected(Objects.equals(option, current))
                .minWidth(width)
                .onClick(() -> this.value.set(option))
                .build());
        }
        return row.build();
    }

    public static class Builder<T> {

        private final List<T> options = new ArrayList<>();
        private Function<? super T, ? extends Component> label = option -> Component.text(String.valueOf(option));
        private @Nullable ValueBinding<T> value;
        private int gap = 2;

        @SafeVarargs
        public final Builder<T> options(final T... options) {
            this.options.addAll(Arrays.asList(options));
            return this;
        }

        public Builder<T> options(final List<T> options) {
            this.options.addAll(options);
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

        public Builder<T> gap(final int gap) {
            this.gap = gap;
            return this;
        }

        public Segmented<T> build() {
            return new Segmented<>(this);
        }
    }
}
