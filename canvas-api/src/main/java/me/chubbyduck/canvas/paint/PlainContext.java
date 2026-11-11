package me.chubbyduck.canvas.paint;

import me.chubbyduck.canvas.node.Node;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Given to {@link Node#plain}, which renders a node for players without the resource pack (or on BedrockDetector). */
@NullMarked
public interface PlainContext {

    /** Renders this node's children in order. */
    void renderChildren();

    void text(Component text);

    void button(Component label, @Nullable Component tooltip, Runnable onClick);

    default void button(final Component label, final Runnable onClick) {
        this.button(label, null, onClick);
    }

    void input(DialogInput input);

    void footer(DialogAction action);

}
