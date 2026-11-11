package me.chubbyduck.canvas.state;

import java.util.function.UnaryOperator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** A piece of element state, created with {@link Scope#useState}. */
@NullMarked
public interface State<T extends @Nullable Object> {

    T get();

    void set(T value);

    default void update(final UnaryOperator<T> updater) {
        this.set(updater.apply(this.get()));
    }

}
