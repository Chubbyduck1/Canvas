package me.chubbyduck.canvas.core.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import me.chubbyduck.canvas.Canvas;
import me.chubbyduck.canvas.asset.NineSlice;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.core.atlas.GlyphAtlas;
import me.chubbyduck.canvas.core.atlas.StyledText;
import me.chubbyduck.canvas.core.state.ScopeImpl;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeInternals;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.paint.DialogAction;
import me.chubbyduck.canvas.paint.DialogInput;
import me.chubbyduck.canvas.paint.PaintContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Resolves custom elements, lays the tree out and paints it into a {@link PaintedFrame}. */
@NullMarked
public class Renderer {

    private static final int Z_BIAS = 1 << 20;

    private final GlyphAtlas atlas;
    private final Predicate<Class<?>> registered;

    public Renderer(final GlyphAtlas atlas, final Predicate<Class<?>> registered) {
        this.atlas = atlas;
        this.registered = registered;
    }

    /** Turns the element tree into nodes, calling {@code getNode} on every custom element. */
    public ResolvedNode resolve(final NodeLike root, final ScopeFactory scopes) {
        return this.resolve(root, "root:" + root.getClass().getName(), scopes);
    }

    private ResolvedNode resolve(final NodeLike like, final String path, final ScopeFactory scopes) {
        final Node node;
        String nodePath = path;
        if (like instanceof final Node direct) {
            node = direct;
        } else {
            final ScopeImpl scope = scopes.scope(path, like.getClass().getName());
            node = like.getNode(scope);
            scope.finish();
            nodePath = path + "/>";
        }
        this.checkRegistered(node.getClass());
        final ResolvedNode resolved = new ResolvedNode(node, nodePath);
        final List<? extends NodeLike> children = node.children();
        for (int i = 0; i < children.size(); i++) {
            final NodeLike child = children.get(i);
            final String key = child.key();
            final String segment = (key != null ? "k" + key : "i" + i) + ":" + child.getClass().getName();
            resolved.children.add(this.resolve(child, nodePath + "/" + segment, scopes));
        }
        return resolved;
    }

    private void checkRegistered(final Class<? extends Node> type) {
        if (NodeInternals.hasDefaultRegister(type)) {
            return;
        }
        final Class<?> owner = NodeInternals.registerOwner(type);
        if (owner != null && !this.registered.test(owner)) {
            throw new IllegalStateException("Node class " + owner.getName()
                + " declares textures (overrides register) but was not registered."
                + " Register it with CanvasRuntime.get().register(" + owner.getSimpleName() + ".class).");
        }
    }

    /** Lays out and paints a resolved tree. */
    public PaintedFrame paint(final ResolvedNode root, final int fixedWidth, final int fixedHeight, final Theme theme, final boolean debug) {
        return this.paint(root, fixedWidth, fixedHeight, 0, 0, theme, debug);
    }

    /**
     * Like {@link #paint(ResolvedNode, int, int, Theme, boolean)}, but a measured (non-fixed) canvas is never smaller
     * than {@code minWidth} x {@code minHeight}.
     */
    public PaintedFrame paint(
        final ResolvedNode root,
        final int fixedWidth,
        final int fixedHeight,
        final int minWidth,
        final int minHeight,
        final Theme theme,
        final boolean debug
    ) {
        final Size measured = this.measure(root, theme, fixedWidth > 0 ? fixedWidth : Canvas.MAX_WIDTH);
        final int width = Math.min(Canvas.MAX_WIDTH, fixedWidth > 0 ? fixedWidth : Math.max(Math.max(1, minWidth), measured.width()));
        final int height = fixedHeight > 0 ? fixedHeight : Math.max(Math.max(1, minHeight), measured.height());
        final Rect canvas = new Rect(0, 0, width, height);
        this.arrange(root, canvas.translate(root.node.offsetX(), root.node.offsetY()), theme);

        final Painter painter = new Painter(canvas, theme);
        this.paintNode(root, 0, painter);
        if (debug) {
            painter.debug(root);
        }
        painter.ops.sort(Comparator.comparingLong(PaintedFrame.DrawOp::order));
        painter.hits.sort(Comparator.comparingLong(PaintedFrame.Hit::order));
        return new PaintedFrame(width, height, painter.ops, painter.hits, painter.inputs, painter.footers, painter.warnings);
    }

    private Size measure(final ResolvedNode node, final Theme theme, final int maxWidth) {
        final Size cached = node.measured.get(maxWidth);
        if (cached != null) {
            return cached;
        }
        final int constraint = node.node.width().isFixed() ? node.node.width().pixels() : maxWidth;
        final Size content = NodeInternals.measure(node.node, new Layout(node, theme, constraint));
        final int width = node.node.width().isFixed() ? node.node.width().pixels() : Math.max(node.node.minWidth(), content.width());
        final int height = node.node.height().isFixed() ? node.node.height().pixels() : Math.max(node.node.minHeight(), content.height());
        final Size size = new Size(width, height);
        node.measured.put(maxWidth, size);
        return size;
    }

    private void arrange(final ResolvedNode node, final Rect rect, final Theme theme) {
        node.rect = rect;
        for (final ResolvedNode child : node.children) {
            child.placed = null;
        }
        NodeInternals.arrange(node.node, rect, new Layout(node, theme, rect.width()));
        for (final ResolvedNode child : node.children) {
            final Rect base = child.placed != null ? child.placed : rect;
            this.arrange(child, base.translate(child.node.offsetX(), child.node.offsetY()), theme);
        }
    }

    private void paintNode(final ResolvedNode node, final int parentZ, final Painter painter) {
        final int z = parentZ + node.node.z();
        painter.current = node;
        painter.z = z;
        NodeInternals.paint(node.node, painter);
        for (final ResolvedNode child : node.children) {
            this.paintNode(child, z, painter);
        }
    }

    /** Creates scopes for custom elements. */
    @FunctionalInterface
    public interface ScopeFactory {

        ScopeImpl scope(String path, String owner);
    }

    private final class Layout implements LayoutContext {

        private final ResolvedNode node;
        private final Theme theme;
        private final int maxWidth;
        private @Nullable List<Child> children;

        Layout(final ResolvedNode node, final Theme theme, final int maxWidth) {
            this.node = node;
            this.theme = theme;
            this.maxWidth = Math.max(0, maxWidth);
        }

        @Override
        public Theme theme() {
            return this.theme;
        }

        @Override
        public int maxWidth() {
            return this.maxWidth;
        }

        @Override
        public List<Child> children() {
            if (this.children == null) {
                final List<Child> list = new ArrayList<>(this.node.children.size());
                for (final ResolvedNode child : this.node.children) {
                    list.add(new Child() {
                        @Override
                        public Node node() {
                            return child.node;
                        }

                        @Override
                        public Size measure() {
                            return Renderer.this.measure(child, Layout.this.theme, Layout.this.maxWidth);
                        }

                        @Override
                        public Size measure(final int width) {
                            return Renderer.this.measure(child, Layout.this.theme, width);
                        }

                        @Override
                        public void place(final Rect rect) {
                            child.placed = rect;
                        }
                    });
                }
                this.children = List.copyOf(list);
            }
            return this.children;
        }

        @Override
        public int textWidth(final Component text) {
            return StyledText.width(text);
        }

        @Override
        public Size spriteSize(final Sprite sprite) {
            return Renderer.this.atlas.spriteSize(sprite.key(), this.theme.id());
        }
    }

    private final class Painter implements PaintContext {

        private final List<PaintedFrame.DrawOp> ops = new ArrayList<>();
        private final List<PaintedFrame.Hit> hits = new ArrayList<>();
        private final List<DialogInput> inputs = new ArrayList<>();
        private final List<DialogAction> footers = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final Rect canvas;
        private final Theme theme;
        // Set before each node paints.
        @SuppressWarnings("NullAway.Init")
        private ResolvedNode current;
        private int z;
        private int sequence;

        Painter(final Rect canvas, final Theme theme) {
            this.canvas = canvas;
            this.theme = theme;
        }

        private long order() {
            final long zPart = Math.clamp((long) this.z + Z_BIAS, 0, 2L * Z_BIAS - 1);
            return (zPart << 32) | (this.sequence++ & 0xFFFFFFFFL);
        }

        private String who() {
            return this.current.node.getClass().getSimpleName() + " at " + this.current.path;
        }

        @Override
        public Rect rect() {
            return this.current.rect;
        }

        @Override
        public Theme theme() {
            return this.theme;
        }

        @Override
        public Rect canvasRect() {
            return this.canvas;
        }

        @Override
        public void sprite(final Sprite sprite, final int x, final int y) {
            final Size size = this.spriteSize(sprite);
            if (x < 0 || x + size.width() > this.canvas.width()) {
                this.warnings.add("Sprite " + sprite.key().asString() + " from " + this.who() + " sticks out of the canvas horizontally and was skipped");
                return;
            }
            this.ops.add(new PaintedFrame.SpriteOp(sprite, x, y, this.order()));
        }

        @Override
        public void nineSlice(final NineSlice skin, final Rect rect) {
            final Rect clipped = rect.intersect(this.canvas);
            if (clipped.isEmpty()) {
                return;
            }
            if (!clipped.equals(rect)) {
                this.warnings.add("Nine-slice from " + this.who() + " was clipped to the canvas");
            }
            this.ops.add(new PaintedFrame.NineOp(skin, clipped, this.order()));
        }

        @Override
        public void text(final Component text, final int x, final int y) {
            if (x < 0) {
                return;
            }
            Component line = text;
            final int available = this.canvas.width() - x;
            if (StyledText.width(line) > available) {
                line = StyledText.fit(line, available);
                this.warnings.add("Text \"" + StyledText.plain(text) + "\" from " + this.who() + " was cut off at the canvas edge");
            }
            final TextColor fallback = TextColor.color(this.theme.palette().text() & 0xFFFFFF);
            int cursor = x;
            for (final StyledText.Run run : StyledText.runs(line)) {
                final ObjectContents object = run.object();
                if (object != null) {
                    if (Math.floorMod(y, 9) != 0) {
                        this.warnings.add("Object in text from " + this.who() + " at y=" + y
                            + " is off the 9px row grid; the client draws it on the nearest row");
                    }
                    this.ops.add(new PaintedFrame.ObjectOp(object, cursor, y, this.order()));
                    cursor += StyledText.OBJECT_ADVANCE;
                    continue;
                }
                final String content = run.text().replace("\n", "");
                if (content.isEmpty()) {
                    continue;
                }
                final Style style = run.style().color() == null ? run.style().color(fallback) : run.style();
                final boolean underlined = style.decoration(TextDecoration.UNDERLINED) == TextDecoration.State.TRUE;
                final boolean struck = style.decoration(TextDecoration.STRIKETHROUGH) == TextDecoration.State.TRUE;
                // The client draws these lines at the text line's position, ignoring the offset
                // font, so they would land up to 8px too high. Draw them as bars instead.
                final Style glyphs = style.decoration(TextDecoration.UNDERLINED, false).decoration(TextDecoration.STRIKETHROUGH, false);
                this.ops.add(new PaintedFrame.TextOp(content, cursor, y, glyphs, this.order()));
                final int width = StyledText.width(new StyledText.Run(content, style));
                // Same extent as vanilla: one pixel left of the run to the end of its advance.
                final int rgb = java.util.Objects.requireNonNullElse(style.color(), fallback).value();
                if (underlined) {
                    this.bar(cursor - 1, y + 8, width + 1, rgb);
                }
                if (struck) {
                    this.bar(cursor - 1, y + 4, width + 1, rgb);
                }
                cursor += width;
            }
        }

        @Override
        public int textWidth(final Component text) {
            return StyledText.width(text);
        }

        @Override
        public Size spriteSize(final Sprite sprite) {
            return Renderer.this.atlas.spriteSize(sprite.key(), this.theme.id());
        }

        @Override
        public void hit(final Rect rect, final Runnable onClick, final @Nullable Component tooltip) {
            final Rect clipped = rect.intersect(this.canvas);
            if (clipped.isEmpty()) {
                return;
            }
            this.hits.add(new PaintedFrame.Hit(clipped, onClick, tooltip, this.order()));
        }

        @Override
        public void input(final DialogInput input) {
            this.inputs.add(input);
        }

        @Override
        public void footer(final DialogAction action) {
            this.footers.add(action);
        }

        private void bar(final int x, final int y, final int width, final int rgb) {
            final int left = Math.max(0, x);
            final int right = Math.min(this.canvas.right(), x + width);
            if (right <= left || y < 0 || y >= this.canvas.bottom()) {
                return;
            }
            this.ops.add(new PaintedFrame.BarOp(left, y, right - left, rgb, this.order()));
        }

        void debug(final ResolvedNode root) {
            if (!DebugBox.isAvailable()) {
                return;
            }
            this.z = Z_BIAS - 2;
            this.debugBoxes(root);
            this.z = Z_BIAS - 1;
            for (final PaintedFrame.Hit hit : List.copyOf(this.hits)) {
                final int top = Math.floorDiv(hit.rect().y(), GlyphAtlas.ROW) * GlyphAtlas.ROW;
                final int bottom = Math.ceilDiv(hit.rect().bottom(), GlyphAtlas.ROW) * GlyphAtlas.ROW;
                final Rect rows = new Rect(hit.rect().x(), top, hit.rect().width(), bottom - top);
                this.current = root;
                this.nineSlice(DebugBox.hitSkin, rows);
            }
        }

        private void debugBoxes(final ResolvedNode node) {
            if (!node.rect.isEmpty()) {
                this.current = node;
                this.nineSlice(DebugBox.boxSkin, node.rect);
            }
            for (final ResolvedNode child : node.children) {
                this.debugBoxes(child);
            }
        }
    }
}
