package me.chubbyduck.canvas.core.session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.core.dialog.CompiledDialog;
import me.chubbyduck.canvas.core.dialog.InputBinding;
import me.chubbyduck.canvas.core.dialog.RenderedDialog;
import me.chubbyduck.canvas.core.render.PaintedFrame;
import me.chubbyduck.canvas.core.render.ResolvedNode;
import me.chubbyduck.canvas.core.state.ScopeImpl;
import me.chubbyduck.canvas.core.state.StateStore;
import me.chubbyduck.canvas.session.CloseHandler;
import me.chubbyduck.canvas.session.CloseReason;
import me.chubbyduck.canvas.session.Session;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * One player's open canvas.
 */
@NullMarked
class SessionImpl implements Session {

    public final String id;
    public final UUID uuid;
    private final SessionManager manager;
    private final Canvas canvas;
    private final Audience viewer;
    private final StateStore store;
    private final Set<String> loggedWarnings = new HashSet<>();
    private volatile boolean open = true;
    private boolean dirty;
    private boolean rendering;
    private final Runnable listener = this::markDirty;
    private int nonce;
    private int shownWidth;
    private int shownHeight;
    private @Nullable RenderedDialog comparable;
    private @Nullable CompiledDialog current;
    private int previousNonce = -1;
    private @Nullable CompiledDialog previous;

    SessionImpl(
        final SessionManager manager,
        final Canvas canvas,
        final Audience viewer,
        final UUID uuid,
        final StateStore store
    ) {
        this.manager = manager;
        this.canvas = canvas;
        this.viewer = viewer;
        this.uuid = uuid;
        this.store = store;
        this.id = Long.toString(ThreadLocalRandom.current().nextLong() & Long.MAX_VALUE, 36);
        store.listen(this.listener);
    }

    @Override
    public Canvas canvas() {
        return this.canvas;
    }

    @Override
    public Audience viewer() {
        return this.viewer;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public void close() {
        this.closeInternal(CloseReason.CLOSED_BY_CODE, true);
    }

    @Override
    public void updateCanvas() {
        this.manager.platform.schedule(this.viewer, () -> this.render(true));
    }

    private void markDirty() {
        synchronized (this) {
            if (!this.open || this.dirty || this.rendering) {
                return;
            }
            this.dirty = true;
        }
        this.manager.platform.schedule(this.viewer, this::flush);
    }

    private void flush() {
        synchronized (this) {
            if (!this.dirty) {
                return;
            }
            this.dirty = false;
        }
        this.renderSafely();
    }

    private void renderSafely() {
        try {
            this.render(false);
        } catch (final RuntimeException exception) {
            this.manager.log("Canvas render failed for " + this.uuid, exception);
        }
    }

    /**
     * Renders and sends the dialog if it changed (or always, when forced).
     */
    synchronized void render(final boolean force) {
        if (!this.open) {
            return;
        }
        this.rendering = true;
        final ResolvedNode root;
        try {
            this.store.beginRender();
            root = this.manager.renderer.resolve(
                this.canvas.root(),
                (path, owner) -> new ScopeImpl(this.store, path, owner, this)
            );
            this.store.endRender();
        } finally {
            this.rendering = false;
        }

        final boolean glyphs = this.manager.platform.supportsGlyphs(this.viewer);
        final PaintedFrame frame;
        final String themeId;
        if (glyphs) {
            final Theme theme = this.manager.themes.apply(this.canvas.theme());
            themeId = theme.id();
            // Never shrink while open: a smaller body would make the client re-center the dialog.
            frame = this.manager.renderer.paint(
                root,
                this.canvas.width(),
                this.canvas.height(),
                this.shownWidth,
                this.shownHeight,
                theme,
                this.manager.debug(this.uuid)
            );
            this.shownWidth = Math.max(this.shownWidth, frame.width());
            this.shownHeight = Math.max(this.shownHeight, frame.height());
            for (final String warning : frame.warnings()) {
                if (this.loggedWarnings.add(warning)) {
                    this.manager.log("[canvas] " + warning, null);
                }
            }
        } else {
            frame = null;
            themeId = "";
        }

        final CompiledDialog probe = this.compile(root, frame, themeId, 0);
        if (!force && probe.dialog().equals(this.comparable)) {
            // Same dialog: nothing to send, but rebind handlers to this render's closures.
            this.current = this.compile(root, frame, themeId, this.nonce);
            return;
        }
        this.comparable = probe.dialog();
        this.previous = this.current;
        this.previousNonce = this.nonce;
        this.nonce++;
        this.current = this.compile(root, frame, themeId, this.nonce);
        this.manager.platform.show(this.viewer, this.current.dialog());
    }

    private CompiledDialog compile(final ResolvedNode root, final @Nullable PaintedFrame frame, final String themeId, final int renderNonce) {
        if (frame != null) {
            return this.manager.glyphs.compile(
                frame, this.canvas.title(), this.canvas.closeOnEscape(), themeId, this.id, renderNonce);
        }
        return this.manager.plain.compile(root, this.canvas.title(), this.canvas.closeOnEscape(), this.id, renderNonce);
    }

    synchronized void click(final int clickNonce, final String action, final InputBinding.@Nullable InputReader inputs) {
        final CompiledDialog compiled;
        if (clickNonce == this.nonce) {
            compiled = this.current;
        } else if (clickNonce == this.previousNonce) {
            compiled = this.previous;
        } else {
            compiled = null;
        }
        if (compiled == null) {
            return;
        }
        if (inputs != null) {
            for (final Map.Entry<String, InputBinding> input : compiled.inputs().entrySet()) {
                input.getValue().apply(inputs, input.getKey());
            }
        }
        if (RenderedDialog.ACTION_CLOSE.equals(action)) {
            this.closeInternal(CloseReason.ESCAPE, true);
            return;
        }
        final Runnable handler = compiled.handlers().get(action);
        if (handler != null) {
            try {
                handler.run();
            } catch (final RuntimeException exception) {
                this.manager.log("Canvas click handler failed for " + this.uuid, exception);
            }
        }
        // One render per click, right away, however many states the handler set.
        this.flush();
    }

    void closeInternal(final CloseReason reason, final boolean sendClose) {
        synchronized (this) {
            if (!this.open) {
                return;
            }
            this.open = false;
            this.dirty = false;
        }
        this.store.unlisten(this.listener);
        this.manager.forget(this);
        if (sendClose) {
            this.manager.platform.close(this.viewer);
        }
        final CloseHandler handler = this.canvas.onClose();
        if (handler == null) {
            return;
        }
        try {
            handler.onClose(this.viewer, reason);
        } catch (final RuntimeException exception) {
            this.manager.log("Canvas onClose handler failed for " + this.uuid, exception);
        }
    }
}
