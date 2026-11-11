package me.chubbyduck.canvas;

import java.util.Optional;
import me.chubbyduck.canvas.session.Session;
import me.chubbyduck.canvas.spi.CanvasPlatform;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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

    static void registerFontWidth(final @Nullable Key font, final int codepoint, final int width) {
        CanvasApi.getPlatform().registerFontWidth(font, codepoint, width);
    }

}
