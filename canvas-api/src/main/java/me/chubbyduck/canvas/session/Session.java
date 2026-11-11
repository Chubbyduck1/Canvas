package me.chubbyduck.canvas.session;

import me.chubbyduck.canvas.Canvas;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;

/**
 * One player's open view of a {@link Canvas}.
 */
@NullMarked
public interface Session {

    Canvas canvas();

    Audience viewer();

    boolean isOpen();

    void close();

    /**
     * Forces a render and re-send, even if no state changed.
     */
    void updateCanvas();

}
