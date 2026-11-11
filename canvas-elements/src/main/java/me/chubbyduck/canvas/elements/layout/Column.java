package me.chubbyduck.canvas.elements.layout;

import me.chubbyduck.canvas.layout.Direction;
import me.chubbyduck.canvas.node.NodeLike;
import org.jspecify.annotations.NullMarked;

/** A vertical {@link Flex}. */
@NullMarked
public final class Column {

    private Column() {
    }

    public static Flex.Builder builder() {
        return Flex.builder().direction(Direction.COLUMN);
    }

    public static Flex of(final NodeLike... children) {
        return builder().add(children).build();
    }
}
