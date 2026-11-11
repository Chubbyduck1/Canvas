package me.chubbyduck.canvas.elements.control;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Composite;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * ◀ value ▶: arrow buttons that step an integer between a minimum and maximum.
 */
@NullMarked
public class Stepper extends Composite {

    private final ValueBinding<Integer> value;
    private final int min;
    private final int max;
    private final int step;
    private final IntFunction<? extends Component> format;
    private final int valueWidth;

    private Stepper(final Builder builder) {
        this.value = Objects.requireNonNull(builder.value, "Stepper needs value(...)");
        this.min = builder.min;
        this.max = builder.max;
        this.step = builder.step;
        this.format = builder.format;
        this.valueWidth = builder.valueWidth;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Node build() {
        final int current = this.value.get();
        return Row.builder()
            .gap(2)
            .align(Align.CENTER)
            .add(ArrowButton.step(this.value, current, this.min, -this.step))
            .add(Text.of(this.format.apply(current)).align(Align.CENTER).width(this.valueWidth))
            .add(ArrowButton.step(this.value, current, this.max, this.step))
            .build();
    }

    public static class Builder {

        private @Nullable ValueBinding<Integer> value;
        private int min;
        private int max = 10;
        private int step = 1;
        private IntFunction<? extends Component> format = Component::text;
        private int valueWidth = 30;

        public Builder value(final State<Integer> state) {
            this.value = ValueBinding.bound(state);
            return this;
        }

        public Builder value(final int value, final Consumer<Integer> onChange) {
            this.value = ValueBinding.controlled(value, onChange);
            return this;
        }

        public Builder range(final int min, final int max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder step(final int step) {
            this.step = Math.max(1, step);
            return this;
        }

        public Builder format(final IntFunction<String> format) {
            this.format = value -> Component.text(format.apply(value));
            return this;
        }

        /**
         * Styled value text.
         */
        public Builder formatComponent(final IntFunction<? extends Component> format) {
            this.format = format;
            return this;
        }

        public Builder valueWidth(final int valueWidth) {
            this.valueWidth = valueWidth;
            return this;
        }

        public Stepper build() {
            return new Stepper(this);
        }
    }
}
