package me.chubbyduck.canvas.core.session;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.core.dialog.GlyphCompiler;
import me.chubbyduck.canvas.core.dialog.InputBinding;
import me.chubbyduck.canvas.core.dialog.PlainCompiler;
import me.chubbyduck.canvas.core.render.Renderer;
import me.chubbyduck.canvas.core.state.StateStore;
import me.chubbyduck.canvas.session.CloseReason;
import me.chubbyduck.canvas.session.OpenMode;
import me.chubbyduck.canvas.session.Session;
import me.chubbyduck.canvas.spi.CanvasPlatform;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Owns every open session and all retained state. */
@NullMarked
public class SessionManager implements CanvasPlatform {

    public final Renderer renderer;
    public final GlyphCompiler glyphs;
    public final PlainCompiler plain;
    public final SessionPlatform platform;
    public final Function<@Nullable Theme, Theme> themes;
    private final Logger logger;

    private final Map<UUID, SessionImpl> sessions = new ConcurrentHashMap<>();
    private final Map<Canvas, Map<UUID, StateStore>> retained = new WeakHashMap<>();
    private final Set<UUID> debug = ConcurrentHashMap.newKeySet();

    public SessionManager(
        final Renderer renderer,
        final GlyphCompiler glyphs,
        final PlainCompiler plain,
        final SessionPlatform platform,
        final Function<@Nullable Theme, Theme> themes,
        final Logger logger
    ) {
        this.renderer = renderer;
        this.glyphs = glyphs;
        this.plain = plain;
        this.platform = platform;
        this.themes = themes;
        this.logger = logger;
    }

    static UUID idOf(final Audience viewer) {
        return viewer.get(Identity.UUID).orElseThrow(
            () -> new IllegalArgumentException("Canvas can only be opened for a player (an audience with a UUID)"));
    }

    @Override
    public Session open(final Canvas canvas, final Audience viewer, final OpenMode mode) {
        final UUID id = idOf(viewer);
        final StateStore store = this.store(canvas, id, mode == OpenMode.FRESH);
        final SessionImpl previous = this.sessions.get(id);
        final SessionImpl session = new SessionImpl(this, canvas, viewer, id, store);
        this.sessions.put(id, session);
        if (previous != null) {
            previous.closeInternal(CloseReason.REPLACED, false);
        }
        try {
            session.render(true);
        } catch (final RuntimeException exception) {
            session.closeInternal(CloseReason.CLOSED_BY_CODE, true);
            throw exception;
        }
        return session;
    }

    @Override
    public void reset(final Canvas canvas, final Audience viewer) {
        final UUID id = idOf(viewer);
        synchronized (this.retained) {
            final Map<UUID, StateStore> stores = this.retained.get(canvas);
            if (stores != null) {
                stores.remove(id);
            }
        }
    }

    @Override
    public Optional<Session> getCurrentSession(final Audience viewer) {
        return viewer.get(Identity.UUID).map(this.sessions::get).filter(SessionImpl::isOpen).map(Session.class::cast);
    }

    private StateStore store(final Canvas canvas, final UUID id, final boolean fresh) {
        synchronized (this.retained) {
            final Map<UUID, StateStore> stores = this.retained.computeIfAbsent(canvas, unused -> new ConcurrentHashMap<>());
            if (fresh) {
                stores.remove(id);
            }
            return stores.computeIfAbsent(id, unused -> new StateStore());
        }
    }

    /** Routes a click from a dialog. */
    public void handleClick(
        final UUID player,
        final String sessionId,
        final int nonce,
        final String action,
        final InputBinding.@Nullable InputReader inputs
    ) {
        final SessionImpl session = this.sessions.get(player);
        if (session == null || !session.isOpen() || !session.id.equals(sessionId)) {
            return;
        }
        session.click(nonce, action, inputs);
    }

    /** The player left: closes their session and drops all state retained for them. */
    public void quit(final UUID player) {
        final SessionImpl session = this.sessions.get(player);
        if (session != null) {
            session.closeInternal(CloseReason.QUIT, false);
        }
        synchronized (this.retained) {
            for (final Map<UUID, StateStore> stores : this.retained.values()) {
                stores.remove(player);
            }
        }
        this.debug.remove(player);
    }

    /** Something else (an inventory, another plugin's dialog) replaced the player's canvas. */
    public void external(final UUID player) {
        final SessionImpl session = this.sessions.get(player);
        if (session != null) {
            session.closeInternal(CloseReason.EXTERNAL, false);
        }
    }

    public void debug(final UUID player, final boolean enabled) {
        if (enabled) {
            this.debug.add(player);
        } else {
            this.debug.remove(player);
        }
        final SessionImpl session = this.sessions.get(player);
        if (session != null) {
            this.platform.schedule(session.viewer(), () -> session.render(true));
        }
    }

    public boolean debug(final UUID player) {
        return this.debug.contains(player);
    }

    /** Re-renders every open session, e.g. after the pack was rebuilt. */
    public void rerenderAll() {
        for (final SessionImpl session : List.copyOf(this.sessions.values())) {
            this.platform.schedule(session.viewer(), () -> session.render(true));
        }
    }

    public Collection<? extends Session> sessions() {
        return List.copyOf(this.sessions.values());
    }

    void forget(final SessionImpl session) {
        this.sessions.remove(session.uuid, session);
    }

    void log(final String message, final @Nullable Throwable error) {
        this.logger.log(error == null ? Level.WARNING : Level.SEVERE, message, error);
    }

}
