package me.chubbyduck.canvas.kitchensink.harness;

import java.util.List;
import java.util.UUID;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.core.dialog.CompiledDialog;
import me.chubbyduck.canvas.core.dialog.GlyphCompiler;
import me.chubbyduck.canvas.core.dialog.PlainCompiler;
import me.chubbyduck.canvas.core.render.PaintedFrame;
import me.chubbyduck.canvas.core.render.Renderer;
import me.chubbyduck.canvas.core.render.ResolvedNode;
import me.chubbyduck.canvas.core.state.ScopeImpl;
import me.chubbyduck.canvas.core.state.StateStore;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.pack.CanvasPack;
import me.chubbyduck.canvas.session.Session;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Renders a page headlessly, without a server: used by {@code /ks selftest} and the golden tests. */
@NullMarked
public final class Harness {

    private final CanvasPack pack;
    private final Renderer renderer;

    public Harness(final CanvasPack pack) {
        this.pack = pack;
        this.renderer = new Renderer(pack.atlas(), pack.registered()::contains);
    }

    /** A stand-in player audience with a fixed UUID. */
    public static Audience fakeViewer() {
        final UUID id = UUID.nameUUIDFromBytes("kitchensink-test".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        final Pointers pointers = Pointers.builder().withStatic(Identity.UUID, id).build();
        return new Audience() {
            @Override
            public Pointers pointers() {
                return pointers;
            }
        };
    }

    public Result render(final NodeLike root, final @Nullable Theme theme, final int width) {
        return this.render(root, theme, width, 0);
    }

    public Result render(final NodeLike root, final @Nullable Theme theme, final int width, final int height) {
        final Canvas canvas = new Canvas(root);
        final Audience viewer = fakeViewer();
        final StateStore store = new StateStore();
        final Session session = new Session() {
            @Override
            public Canvas canvas() {
                return canvas;
            }

            @Override
            public Audience viewer() {
                return viewer;
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() {
            }

            @Override
            public void updateCanvas() {
            }
        };
        store.beginRender();
        final ResolvedNode resolved = this.renderer.resolve(root, (path, owner) -> new ScopeImpl(store, path, owner, session));
        store.endRender();
        if (theme == null) {
            final CompiledDialog compiled = new PlainCompiler().compile(resolved, Component.text("test"), true, "test", 1);
            return new Result(compiled, null);
        }
        final PaintedFrame frame = this.renderer.paint(resolved, width, height, theme, false);
        final CompiledDialog compiled = new GlyphCompiler(this.pack.atlas())
            .compile(frame, Component.text("test"), true, theme.id(), "test", 1);
        return new Result(compiled, frame);
    }

    public CanvasPack pack() {
        return this.pack;
    }

    public record Result(CompiledDialog compiled, @Nullable PaintedFrame frame) {

        public List<String> warnings() {
            return this.frame == null ? List.of() : this.frame.warnings();
        }
    }
}
