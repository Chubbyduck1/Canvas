package me.chubbyduck.canvas.core.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** All state of one (canvas, player) pair: a list of slots per element path. */
@NullMarked
public class StateStore {

    private final Map<String, Cells> cells = new HashMap<>();
    private final Set<String> touched = new HashSet<>();
    private volatile @Nullable Runnable listener;

    /** Sets the callback that runs when any value changes (the live session's re-render trigger). */
    public synchronized void listen(final @Nullable Runnable listener) {
        this.listener = listener;
    }

    /** Removes the callback, but only if it is still the given one (a newer session may have taken over this store). */
    public synchronized void unlisten(final Runnable listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    synchronized Cells cells(final String path, final String owner) {
        this.touched.add(path);
        return this.cells.computeIfAbsent(path, unused -> new Cells(owner));
    }

    /**
     * Starts a render pass: paths not used during the pass are dropped at {@link #endRender()}, like unmounted
     * components.
     */
    public synchronized void beginRender() {
        this.touched.clear();
    }

    public synchronized void endRender() {
        this.cells.keySet().retainAll(this.touched);
    }

    synchronized @Nullable Object get(final Cells cells, final int index) {
        return cells.values.get(index);
    }

    void set(final Cells cells, final int index, final @Nullable Object value) {
        synchronized (this) {
            if (java.util.Objects.equals(cells.values.get(index), value)) {
                return;
            }
            cells.values.set(index, value);
        }
        final Runnable current = this.listener;
        if (current != null) {
            current.run();
        }
    }

    /** The state slots of one element. */
    static class Cells {

        public final String owner;
        public final List<@Nullable Object> values = new ArrayList<>();
        public int expectedHooks = -1;

        Cells(final String owner) {
            this.owner = owner;
        }
    }
}
