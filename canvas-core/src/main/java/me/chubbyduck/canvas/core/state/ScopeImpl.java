package me.chubbyduck.canvas.core.state;

import java.util.function.Supplier;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.session.Session;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Scope of one custom element during one render.
 */
@NullMarked
public class ScopeImpl implements Scope {

    private final StateStore store;
    private final StateStore.Cells cells;
    private final Session session;
    private int hook;
    private boolean finished;

    public ScopeImpl(final StateStore store, final String path, final String owner, final Session session) {
        this.store = store;
        this.cells = store.cells(path, owner);
        this.session = session;
    }

    @Override
    public <T extends @Nullable Object> State<T> useState(final T initial) {
        return this.useState(() -> initial);
    }

    @Override
    public <T extends @Nullable Object> State<T> useState(final Supplier<T> initial) {
        if (this.finished) {
            throw new IllegalStateException(
                "useState called after getNode returned in " + this.cells.owner
                    + ". Create state at the top of getNode, not inside click handlers.");
        }
        final int index = this.hook++;
        synchronized (this.store) {
            final boolean newHook = index >= this.cells.values.size();
            if (newHook && this.cells.expectedHooks >= 0) {
                throw this.orderError(index + 1);
            }
            if (newHook) {
                this.cells.values.add(initial.get());
            }
        }
        return new StateImpl<>(this.store, this.cells, index);
    }

    @Override
    public Audience viewer() {
        return this.session.viewer();
    }

    @Override
    public Canvas canvas() {
        return this.session.canvas();
    }

    @Override
    public Session session() {
        return this.session;
    }

    /**
     * Called after {@code getNode} returns; checks the hook count matches previous renders.
     */
    public void finish() {
        this.finished = true;
        if (this.cells.expectedHooks < 0) {
            this.cells.expectedHooks = this.hook;
            return;
        }
        if (this.cells.expectedHooks != this.hook) {
            throw this.orderError(this.hook);
        }
    }

    private IllegalStateException orderError(final int actual) {
        return new IllegalStateException(
            this.cells.owner + " called useState " + actual + " times, but " + this.cells.expectedHooks
                + " times on an earlier render. useState must be called the same number of times, in the same"
                + " order, on every render (never inside a condition or loop).");
    }
}
