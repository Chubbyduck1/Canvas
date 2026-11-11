package me.chubbyduck.canvas;

import java.util.Optional;
import me.chubbyduck.canvas.session.Session;
import me.chubbyduck.canvas.spi.CanvasPlatform;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface CanvasApi {

    static CanvasPlatform getPlatform() {
        return CanvasPlatform.get();
    }

    static Optional<Session> getCurrentSession(final Audience viewer) {
        return CanvasApi.getPlatform().getCurrentSession(viewer);
    }

    static Optional<Canvas> getCurrentCanvas(final Audience viewer) {
        return CanvasApi.getCurrentSession(viewer).map(Session::canvas);
    }

    static void updateCurrentCanvas(final Audience viewer) {
        CanvasApi.getCurrentSession(viewer).ifPresent(Session::updateCanvas);
    }

}
