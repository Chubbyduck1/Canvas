package me.chubbyduck.canvas.elements.control;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import me.chubbyduck.canvas.core.atlas.StyledText;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.layout.Composite;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.elements.layout.Row;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** A glyph slider: optional value label, ◀ arrow, clickable track, ▶ arrow. */
@NullMarked
public class Slider extends Composite {

    private final ValueBinding<Integer> value;
    private final int min;
    private final int max;
    private final int step;
    private final @Nullable IntFunction<? extends Component> format;
    private final int trackWidth;
    private final boolean arrows;

    private Slider(final Builder builder) {
        this.value = Objects.requireNonNull(builder.value, "Slider needs value(...)");
        this.min = builder.min;
        this.max = builder.max;
        this.step = builder.step;
        this.format = builder.format;
        this.trackWidth = builder.trackWidth;
        this.arrows = builder.arrows;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Node build() {
        final int current = this.value.get();
        final Flex.Builder row = Row.builder().gap(2).align(Align.CENTER);
        if (this.format != null) {
            // As wide as the widest value in the range, so the track doesn't move while dragging.
            int labelWidth = 0;
            for (long candidate = this.min; candidate <= this.max; candidate += this.step) {
                labelWidth = Math.max(labelWidth, StyledText.width(this.format.apply((int) candidate)));
            }
            row.add(Text.of(this.format.apply(current)).align(Align.END).width(labelWidth));
        }
        if (this.arrows) {
            row.add(ArrowButton.step(this.value, current, this.min, -this.step));
        }
        row.add(new SliderTrack(this.value, this.min, this.max, this.step, this.trackWidth));
        if (this.arrows) {
            row.add(ArrowButton.step(this.value, current, this.max, this.step));
        }
        return row.build();
    }

    public static class Builder {

        private @Nullable ValueBinding<Integer> value;
        private int min;
        private int max = 100;
        private int step = 1;
        private @Nullable IntFunction<? extends Component> format;
        private int trackWidth = 80;
        private boolean arrows = true;

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

        /** Shows the value to the left of the slider, e.g. {@code value -> value + "%"}. */
        public Builder format(final @Nullable IntFunction<String> format) {
            this.format = format == null ? null : value -> Component.text(format.apply(value));
            return this;
        }

        /** Styled value label, e.g. a number in a color. */
        public Builder formatComponent(final @Nullable IntFunction<? extends Component> format) {
            this.format = format;
            return this;
        }

        public Builder trackWidth(final int trackWidth) {
            this.trackWidth = trackWidth;
            return this;
        }

        public Builder arrows(final boolean arrows) {
            this.arrows = arrows;
            return this;
        }

        public Slider build() {
            return new Slider(this);
        }
    }
}
