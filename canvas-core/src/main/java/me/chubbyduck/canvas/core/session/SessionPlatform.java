package me.chubbyduck.canvas.core.session;

import me.chubbyduck.canvas.core.dialog.RenderedDialog;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;

/** What the session manager needs from the server platform. */
@NullMarked
public interface SessionPlatform {

    /** Shows (or replaces) the viewer's dialog. */
    void show(Audience viewer, RenderedDialog dialog);

    /** Closes the viewer's dialog. */
    void close(Audience viewer);

    /** Runs a task on the viewer's thread (next tick, or immediately if possible). */
    void schedule(Audience viewer, Runnable task);

    /** Whether the viewer can see glyph canvases (has the pack, isn't on BedrockDetector, didn't opt out). */
    boolean supportsGlyphs(Audience viewer);
}
