package me.chubbyduck.canvas;

import java.util.Objects;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.session.CloseHandler;
import me.chubbyduck.canvas.session.OpenMode;
import me.chubbyduck.canvas.session.Session;
import me.chubbyduck.canvas.spi.CanvasPlatform;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** The root element, plus options. */
@NullMarked
public class Canvas {

    /** Widest canvas in GUI pixels: a 400px screen minus the dialog's 20px scroll area and 9px padding. */
    public static final int MAX_WIDTH = 400 - (20 + 9);

    private final NodeLike root;
    private Component title = Component.empty();
    private int width;
    private int height;
    private @Nullable Theme theme;
    private boolean closeOnEscape = true;
    private @Nullable CloseHandler onClose;

    public Canvas(final NodeLike root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    public Canvas title(final Component title) {
        this.title = Objects.requireNonNull(title, "title");
        return this;
    }

    /** Fixes the canvas size in GUI pixels. */
    public Canvas size(final int width, final int height) {
        if (width < 1 || height < 1 || width > MAX_WIDTH) {
            throw new IllegalArgumentException("Canvas size must be 1.." + MAX_WIDTH + " wide and at least 1 tall");
        }
        this.width = width;
        this.height = height;
        return this;
    }

    public Canvas theme(final @Nullable Theme theme) {
        this.theme = theme;
        return this;
    }

    public Canvas closeOnEscape(final boolean closeOnEscape) {
        this.closeOnEscape = closeOnEscape;
        return this;
    }

    public Canvas onClose(final @Nullable CloseHandler onClose) {
        this.onClose = onClose;
        return this;
    }

    public Session open(final Audience viewer) {
        return this.open(viewer, OpenMode.RESUME);
    }

    public Session open(final Audience viewer, final OpenMode mode) {
        return CanvasPlatform.get().open(this, viewer, mode);
    }

    /** Discards the state retained for this viewer. */
    public void reset(final Audience viewer) {
        CanvasPlatform.get().reset(this, viewer);
    }

    public NodeLike root() {
        return this.root;
    }

    public Component title() {
        return this.title;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    @Nullable
    public Theme theme() {
        return this.theme;
    }

    public boolean closeOnEscape() {
        return this.closeOnEscape;
    }

    @Nullable
    public CloseHandler onClose() {
        return this.onClose;
    }

}
