package me.chubbyduck.canvas.elements.display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.NineSlice;
import me.chubbyduck.canvas.elements.layout.Column;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.elements.skin.OreSkins;
import me.chubbyduck.canvas.layout.Align;
import me.chubbyduck.canvas.layout.Insets;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An OreUI panel: an outlined surface with an optional header strip.
 */
@NullMarked
public class Panel extends Node {

    /**
     * Header strip height: outline, edge, 15px face, two-tone rule.
     */
    private static final int HEADER_HEIGHT = 20;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static NineSlice bodySkin;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static NineSlice headerSkin;
    private final @Nullable Component title;
    private final Flex content;
    private final int headerHeight;

    private Panel() {
        this.title = null;
        this.content = Column.builder().build();
        this.headerHeight = 0;
    }

    private Panel(final Builder builder) {
        this.title = builder.title;
        this.content = Column.builder()
            .gap(builder.gap)
            .padding(builder.padding)
            .align(Align.STRETCH)
            .addAll(builder.children)
            .build();
        this.headerHeight = builder.title == null ? 0 : HEADER_HEIGHT;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<? extends NodeLike> children() {
        return List.of(this.content);
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        bodySkin = assets.registerNineSlice(Key.key("canvas", "panel/body"), OreSkins.panel());
        headerSkin = assets.registerNineSlice(Key.key("canvas", "panel/header"), OreSkins.panelHeader());
    }

    @Override
    protected Size measure(final LayoutContext context) {
        final Insets frame = this.frame();
        final int available = context.maxWidth() == Integer.MAX_VALUE
            ? Integer.MAX_VALUE
            : Math.max(0, context.maxWidth() - frame.horizontal());
        final Size inner = context.children().getFirst().measure(available).grow(frame);
        final int titleWidth = this.title == null ? 0 : context.textWidth(this.title) + 12;
        return new Size(Math.max(inner.width(), titleWidth), inner.height());
    }

    @Override
    protected void arrange(final Rect rect, final LayoutContext context) {
        context.children().getFirst().place(rect.inset(this.frame()));
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        context.nineSlice(bodySkin, rect);
        if (this.title == null) {
            return;
        }
        context.nineSlice(headerSkin, new Rect(rect.x(), rect.y(), rect.width(), this.headerHeight));
        // Centered on the header face (between the top outline and the two-tone rule).
        context.text(this.title, rect.x() + 6, rect.y() + 1 + (this.headerHeight - 3 - 8) / 2, context.theme().palette().text());
    }

    @Override
    protected void plain(final PlainContext context) {
        if (this.title != null) {
            context.text(Component.text().decorate(TextDecoration.BOLD).append(this.title).build());
        }
        context.renderChildren();
    }

    /**
     * Space the card's frame takes on each side: 1px outline plus the light/dark edge on the top and sides, and
     * outline, 2px lip and rule line at the bottom.
     */
    private Insets frame() {
        return Insets.of(this.headerHeight > 0 ? this.headerHeight : 2, 2, 4, 2);
    }

    public static class Builder {

        private final List<NodeLike> children = new ArrayList<>();
        private @Nullable Component title;
        private Insets padding = Insets.all(4);
        private int gap = 6;

        public Builder title(final @Nullable String title) {
            this.title = title == null ? null : Component.text(title);
            return this;
        }

        /**
         * A styled header title.
         */
        public Builder title(final @Nullable Component title) {
            this.title = title;
            return this;
        }

        public Builder padding(final int padding) {
            this.padding = Insets.all(padding);
            return this;
        }

        public Builder padding(final Insets padding) {
            this.padding = padding;
            return this;
        }

        public Builder gap(final int gap) {
            this.gap = gap;
            return this;
        }

        public Builder add(final NodeLike... children) {
            this.children.addAll(Arrays.asList(children));
            return this;
        }

        public Builder addAll(final List<? extends NodeLike> children) {
            this.children.addAll(children);
            return this;
        }

        public Panel build() {
            return new Panel(this);
        }
    }
}
