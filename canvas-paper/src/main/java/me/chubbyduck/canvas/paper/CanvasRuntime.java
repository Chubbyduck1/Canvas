package me.chubbyduck.canvas.paper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.core.dialog.GlyphCompiler;
import me.chubbyduck.canvas.core.dialog.PlainCompiler;
import me.chubbyduck.canvas.core.render.DebugBox;
import me.chubbyduck.canvas.core.render.Renderer;
import me.chubbyduck.canvas.core.session.SessionManager;
import me.chubbyduck.canvas.elements.Themes;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.pack.CanvasPack;
import me.chubbyduck.canvas.pack.CanvasPackBuilder;
import me.chubbyduck.canvas.paper.platform.PaperPlatform;
import me.chubbyduck.canvas.session.Session;
import me.chubbyduck.canvas.spi.CanvasPlatform;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Runs Canvas inside a plugin: builds the resource pack into {@code <data folder>/pack.zip} and installs the runtime
 * behind {@code new Canvas(...).open(player)}.
 */
@NullMarked
public class CanvasRuntime {

    private static final String PACK_FILE = "pack.zip";

    private static volatile @Nullable CanvasRuntime current;

    private final JavaPlugin plugin;
    private final List<Theme> themes;
    private final Set<Class<? extends Node>> classes;
    private final PaperPlatform platform;
    private volatile CanvasPack pack;
    private volatile @Nullable SessionManager sessions;
    private boolean rebuildScheduled;

    private CanvasRuntime(
        final JavaPlugin plugin,
        final List<Theme> themes,
        final Set<Class<? extends Node>> classes,
        final boolean requirePack
    ) {
        this.plugin = plugin;
        this.themes = themes;
        this.classes = classes;
        this.platform = new PaperPlatform(plugin, requirePack);
        this.pack = this.rebuild();
    }

    public static Builder bootstrap(final JavaPlugin plugin) {
        return new Builder(plugin);
    }

    public static CanvasRuntime get() {
        final CanvasRuntime runtime = current;
        if (runtime == null) {
            throw new IllegalStateException("Canvas is not started");
        }
        return runtime;
    }

    @SafeVarargs
    public final void register(final Class<? extends Node>... classes) {
        final List<Class<? extends Node>> list = new ArrayList<>(classes.length);
        list.addAll(Arrays.asList(classes));
        this.register(list);
    }

    public void register(final List<Class<? extends Node>> classes) {
        if (!this.classes.addAll(classes)) {
            return;
        }
        this.scheduleRebuild();
    }

    public void registerTheme(final Theme... themes) {
        for (final Theme theme : themes) {
            if (this.themes.stream().anyMatch(registered -> registered.id().equals(theme.id()))) {
                continue;
            }
            this.themes.add(theme);
            this.scheduleRebuild();
        }
    }

    public void reload() {
        this.pack = this.rebuild();
    }

    private void scheduleRebuild() {
        if (this.rebuildScheduled) {
            return;
        }
        this.rebuildScheduled = true;
        Bukkit.getGlobalRegionScheduler().run(this.plugin, task -> {
            this.rebuildScheduled = false;
            this.reload();
        });
    }

    private CanvasPack rebuild() {
        final CanvasPackBuilder builder = new CanvasPackBuilder().registerTheme(this.themes.toArray(Theme[]::new));
        for (final Class<? extends Node> type : this.classes) {
            builder.register(type);
        }
        builder.register(DebugBox.class);
        final CanvasPack built = builder.build();
        final Path file = this.plugin.getDataFolder().toPath().resolve(PACK_FILE);
        try {
            built.writeTo(file);
        } catch (final IOException exception) {
            throw new UncheckedIOException("Could not write the Canvas pack to " + file, exception);
        }

        final SessionManager previous = this.sessions;
        if (previous != null) {
            // Canvases rendered with the old atlas may use glyphs that no longer exist.
            for (final Session session : previous.sessions()) {
                session.close();
            }
        }
        final SessionManager manager = new SessionManager(
            new Renderer(built.atlas(), built.registered()::contains),
            new GlyphCompiler(built.atlas()),
            new PlainCompiler(),
            this.platform,
            this::resolveTheme,
            this.plugin.getLogger()
        );
        this.sessions = manager;
        CanvasPlatform.install(manager);
        this.plugin.getLogger().info(String.format(Locale.ROOT, "Canvas pack written to %s (%d KiB, sha1 %s)",
            file, built.zip().length / 1024, built.sha1()));
        return built;
    }

    private Theme resolveTheme(final @Nullable Theme theme) {
        if (theme == null) {
            return this.themes.getFirst();
        }
        for (final Theme registered : this.themes) {
            if (registered.id().equals(theme.id())) {
                return registered;
            }
        }
        throw new IllegalStateException("Theme " + theme.id() + " is not registered");
    }

    public void stop() {
        if (current != this) {
            return;
        }
        for (final Session session : this.sessions().sessions()) {
            session.close();
        }
        CanvasPlatform.install(null);
        current = null;
    }

    public CanvasPack pack() {
        return this.pack;
    }

    public SessionManager sessions() {
        return Objects.requireNonNull(this.sessions, "Canvas sessions are not initialized");
    }

    public void forcePlain(final Player player, final boolean plain) {
        this.platform.forcePlain(player.getUniqueId(), plain);
        this.rerender(player);
    }

    public void debug(final Player player, final boolean enabled) {
        this.sessions().debug(player.getUniqueId(), enabled);
    }

    PaperPlatform platform() {
        return this.platform;
    }

    JavaPlugin plugin() {
        return this.plugin;
    }

    void rerender(final Player player) {
        this.sessions().getCurrentSession(player).ifPresent(Session::updateCanvas);
    }

    public static class Builder {

        private final JavaPlugin plugin;
        private final List<Theme> themes = new ArrayList<>();
        private final Set<Class<? extends Node>> classes = new LinkedHashSet<>();
        private boolean requirePack;

        private Builder(final JavaPlugin plugin) {
            this.plugin = plugin;
        }

        public Builder registerTheme(final Theme... themes) {
            this.themes.addAll(Arrays.asList(themes));
            return this;
        }

        @SafeVarargs
        public final Builder register(final Class<? extends Node>... classes) {
            this.classes.addAll(Arrays.asList(classes));
            return this;
        }

        public Builder register(final List<Class<? extends Node>> classes) {
            this.classes.addAll(classes);
            return this;
        }

        /**
         * Whether players only get glyph dialogs after a resource pack has loaded; off by default.
         */
        public Builder requirePack(final boolean requirePack) {
            this.requirePack = requirePack;
            return this;
        }

        public CanvasRuntime start() {
            if (current != null) {
                throw new IllegalStateException("Canvas is already started (by another plugin?)");
            }
            if (this.themes.isEmpty()) {
                this.themes.add(Themes.ORE);
            }
            final CanvasRuntime runtime = new CanvasRuntime(this.plugin, this.themes, this.classes, this.requirePack);
            current = runtime;
            Bukkit.getPluginManager().registerEvents(new CanvasListener(runtime), this.plugin);
            return runtime;
        }
    }
}
