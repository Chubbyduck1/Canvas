package me.chubbyduck.canvas.node;

import me.chubbyduck.canvas.state.Scope;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Anything that can be placed in a canvas tree.
 */
@FunctionalInterface
@NullMarked
public interface NodeLike {

    /**
     * Builds (or returns) the node this element renders as.
     */
    Node getNode(Scope scope);

    /**
     * An optional key that identifies this element among its siblings, so it keeps its state when the sibling list is
     * reordered.
     */
    default @Nullable String key() {
        return null;
    }

}
