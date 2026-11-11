package me.chubbyduck.canvas.core.state;

import java.util.Objects;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** A handle to one state slot. */
@NullMarked
class StateImpl<T extends @Nullable Object> implements State<T> {

    private final StateStore store;
    private final StateStore.Cells cells;
    private final int index;

    StateImpl(final StateStore store, final StateStore.Cells cells, final int index) {
        this.store = store;
        this.cells = cells;
        this.index = index;
    }

    @Override
    // The slot holds the T this state was created with (null only if T is nullable).
    @SuppressWarnings({"unchecked", "NullAway"})
    public T get() {
        return (T) this.store.get(this.cells, this.index);
    }

    @Override
    public void set(final T value) {
        this.store.set(this.cells, this.index, value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(System.identityHashCode(this.cells), this.index);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof final StateImpl<?> that && that.cells == this.cells && that.index == this.index;
    }

    @Override
    public String toString() {
        return "State[" + this.cells.owner + "#" + this.index + "=" + this.get() + "]";
    }
}
