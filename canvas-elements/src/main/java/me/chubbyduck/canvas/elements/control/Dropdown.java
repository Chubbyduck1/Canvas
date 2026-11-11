package me.chubbyduck.canvas.elements.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A drawn dropdown: a button showing the current choice; clicking it opens a list of options (with optional icons) over
 * the content below.
 */
@NullMarked
public class Dropdown<T> implements NodeLike {

    private final List<T> options;
    private final Function<? super T, ? extends Component> label;
    private final @Nullable Function<T, Sprite> icon;
    private final ValueBinding<T> value;
    private final int width;
    private final @Nullable String key;

    private Dropdown(final Builder<T> builder) {
        this.options = List.copyOf(builder.options);
        this.label = builder.label;
        this.icon = builder.icon;
        this.value = Objects.requireNonNull(builder.value, "Dropdown needs value(...)");
        this.width = builder.width;
        this.key = builder.key;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    public Node getNode(final Scope scope) {
        final State<Boolean> open = scope.useState(false);
        final T current = this.value.get();
        final List<DropdownNode.Option> rows = new ArrayList<>(this.options.size());
        for (final T option : this.options) {
            rows.add(new DropdownNode.Option(
                this.label.apply(option),
                this.icon == null ? null : this.icon.apply(option),
                Objects.equals(option, current),
                () -> {
                    this.value.set(option);
                    open.set(false);
                }
            ));
        }
        final DropdownNode.Option selected = rows.stream().filter(DropdownNode.Option::selected).findFirst().orElse(null);
        return new DropdownNode(rows, selected, open.get(), () -> open.set(!open.get()), () -> open.set(false), this.width);
    }

    @Override
    public @Nullable String key() {
        return this.key;
    }

    public static class Builder<T> {

        private final List<T> options = new ArrayList<>();
        private Function<? super T, ? extends Component> label = option -> Component.text(String.valueOf(option));
        private @Nullable Function<T, Sprite> icon;
        private @Nullable ValueBinding<T> value;
        private int width;
        private @Nullable String key;

        @SafeVarargs
        public final Builder<T> options(final T... options) {
            for (final T value : options) {
                this.options.add(value);
            }
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

        /** Styled labels. */
        public Builder<T> labelComponent(final Function<? super T, ? extends Component> label) {
            this.label = label;
            return this;
        }

        /** An icon for each option, drawn left of its label (e.g. a flag). */
        public Builder<T> icon(final @Nullable Function<T, Sprite> icon) {
            this.icon = icon;
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

        /** Fixed width; by default the widest option decides. */
        public Builder<T> width(final int width) {
            this.width = width;
            return this;
        }

        /** Identifies this dropdown among its siblings so its open state follows it. */
        public Builder<T> key(final @Nullable String key) {
            this.key = key;
            return this;
        }

        public Dropdown<T> build() {
            return new Dropdown<>(this);
        }
    }
}
