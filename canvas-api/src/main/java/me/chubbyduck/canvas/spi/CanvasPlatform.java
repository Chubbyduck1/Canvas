package me.chubbyduck.canvas.spi;

import java.util.Optional;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.session.OpenMode;
import me.chubbyduck.canvas.session.Session;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Implemented by the runtime that actually shows canvases. */
@NullMarked
public interface CanvasPlatform {

    static CanvasPlatform get() {
        final CanvasPlatform platform = Holder.instance;
        if (platform == null) {
            throw new IllegalStateException(
                "Canvas is not started. Install the Canvas plugin, or call CanvasRuntime.bootstrap(plugin)...start() in onEnable.");
        }
        return platform;
    }

    static void install(final @Nullable CanvasPlatform platform) {
        Holder.instance = platform;
    }

    Session open(Canvas canvas, Audience viewer, OpenMode mode);

    void reset(Canvas canvas, Audience viewer);

    Optional<Session> getCurrentSession(Audience viewer);

    final class Holder {

        private static volatile @Nullable CanvasPlatform instance;

        private Holder() {
        }

    }
}
