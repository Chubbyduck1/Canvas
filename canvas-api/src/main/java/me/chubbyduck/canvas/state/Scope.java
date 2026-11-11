package me.chubbyduck.canvas.state;

import java.util.function.Supplier;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.session.Session;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Passed to {@link NodeLike#getNode(Scope)}. */
@NullMarked
public interface Scope {

    <T extends @Nullable Object> State<T> useState(T initial);

    <T extends @Nullable Object> State<T> useState(Supplier<T> initial);

    Audience viewer();

    Canvas canvas();

    Session session();

}
