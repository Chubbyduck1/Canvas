package me.chubbyduck.canvas.elements.control;

import java.util.function.Consumer;
import java.util.function.Supplier;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;

/**
 * A value a control shows and changes: either a bound {@link State} (two-way) or a plain value plus an {@code onChange}
 * callback (controlled).
 */
@NullMarked
public final class ValueBinding<T> {

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    private ValueBinding(final Supplier<T> getter, final Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public static <T> ValueBinding<T> bound(final State<T> state) {
        return new ValueBinding<>(state::get, state::set);
    }

    public static <T> ValueBinding<T> controlled(final T value, final Consumer<T> onChange) {
        return new ValueBinding<>(() -> value, onChange);
    }

    public T get() {
        return this.getter.get();
    }

    public void set(final T value) {
        this.setter.accept(value);
    }
}
