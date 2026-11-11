package me.chubbyduck.canvas.core.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** A node after its custom elements were resolved, with layout results. */
@NullMarked
public class ResolvedNode {

    public final Node node;
    public final String path;
    public final List<ResolvedNode> children = new ArrayList<>();
    public final Map<Integer, Size> measured = new HashMap<>();
    @Nullable
    public Rect placed;
    public Rect rect = Rect.EMPTY;

    public ResolvedNode(final Node node, final String path) {
        this.node = node;
        this.path = path;
    }

    public Node node() {
        return this.node;
    }

    public String path() {
        return this.path;
    }

    public List<ResolvedNode> children() {
        return this.children;
    }

    public Rect rect() {
        return this.rect;
    }
}
