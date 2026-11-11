package me.chubbyduck.canvas.elements.control;

import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.NineSlice;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.elements.skin.OreSkins;
import me.chubbyduck.canvas.elements.skin.Variant;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A raised OreUI button with a label and/or icon, drawn entirely with Canvas glyphs.
 */
@NullMarked
public class Button extends Node {

    private static final int PADDING = 8;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static NineSlice normalSkin;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static NineSlice selectedSkin;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static NineSlice disabledSkin;
    private final @Nullable Component label;
    private final @Nullable Sprite icon;
    private final Runnable onClick;
    private final boolean selected;
    private final boolean disabled;
    private final @Nullable Component tooltip;
    private final int minWidth;

    protected Button() {
        this(new Builder());
    }

    protected Button(final Builder builder) {
        this.label = builder.label;
        this.icon = builder.icon;
        this.onClick = builder.onClick;
        this.selected = builder.selected;
        this.disabled = builder.disabled;
        this.tooltip = builder.tooltip;
        this.minWidth = builder.minWidth;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Button of(final String label, final Runnable onClick) {
        return builder().label(label).onClick(onClick).build();
    }

    public static Button of(final Component label, final Runnable onClick) {
        return builder().label(label).onClick(onClick).build();
    }

    static NineSlice skin(final boolean selected, final boolean disabled) {
        if (disabled) {
            return disabledSkin;
        }
        return selected ? selectedSkin : normalSkin;
    }

    static int textColor(final PaintContext context, final boolean selected, final boolean disabled) {
        if (disabled) {
            return context.theme().palette().textDisabled();
        }
        return selected ? context.theme().palette().textOnAccent() : context.theme().palette().text();
    }

    static Component selectedLabel(final Component label, final boolean selected) {
        return selected ? Component.text("> ").append(label).append(Component.text(" <")) : label;
    }

    private static void nothing() {
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        normalSkin = assets.registerNineSlice(Key.key("canvas", "button/normal"), OreSkins.button(Variant.NORMAL));
        selectedSkin = assets.registerNineSlice(Key.key("canvas", "button/selected"), OreSkins.button(Variant.SELECTED));
        disabledSkin = assets.registerNineSlice(Key.key("canvas", "button/disabled"), OreSkins.button(Variant.DISABLED));
    }

    @Override
    protected Size measure(final LayoutContext context) {
        int content = 0;
        if (this.icon != null) {
            content += context.spriteSize(this.icon).width();
        }
        if (this.label != null) {
            content += (content > 0 ? 4 : 0) + context.textWidth(this.label);
        }
        final int width = Math.max(this.minWidth, Math.max(20, content + 2 * PADDING));
        return new Size(width, context.theme().metrics().buttonHeight());
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        context.nineSlice(skin(this.selected, this.disabled), rect);

        final int iconWidth = this.icon == null ? 0 : context.spriteSize(this.icon).width();
        final int textWidth = this.label == null ? 0 : context.textWidth(this.label);
        final int content = iconWidth + textWidth + (iconWidth > 0 && textWidth > 0 ? 4 : 0);
        int x = rect.x() + (rect.width() - content) / 2;
        // Center on the face: inside the outline and above the 2px lip.
        if (this.icon != null) {
            final Size size = context.spriteSize(this.icon);
            context.sprite(this.icon, x, rect.y() + OreSkins.buttonContentY(rect.height(), size.height()));
            x += iconWidth + (textWidth > 0 ? 4 : 0);
        }
        if (this.label != null) {
            context.text(this.label, x, rect.y() + OreSkins.buttonContentY(rect.height(), 8), textColor(context, this.selected, this.disabled));
        }
        if (!this.disabled) {
            context.hit(rect, this.onClick, this.tooltip);
        }
    }

    @Override
    protected void plain(final PlainContext context) {
        if (this.disabled) {
            return;
        }
        final Component label = this.label == null ? Component.text("...") : this.label;
        context.button(selectedLabel(label, this.selected), this.tooltip, this.onClick);
    }

    public static class Builder {

        private @Nullable Component label;
        private @Nullable Sprite icon;
        private Runnable onClick = Button::nothing;
        private boolean selected;
        private boolean disabled;
        private @Nullable Component tooltip;
        private int minWidth;

        public Builder label(final @Nullable String label) {
            this.label = label == null ? null : Component.text(label);
            return this;
        }

        /**
         * A styled label.
         */
        public Builder label(final @Nullable Component label) {
            this.label = label;
            return this;
        }

        public Builder icon(final @Nullable Sprite icon) {
            this.icon = icon;
            return this;
        }

        public Builder onClick(final Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public Builder selected(final boolean selected) {
            this.selected = selected;
            return this;
        }

        public Builder disabled(final boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public Builder tooltip(final @Nullable String tooltip) {
            this.tooltip = tooltip == null ? null : Component.text(tooltip);
            return this;
        }

        public Builder tooltip(final @Nullable Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder minWidth(final int minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        public Button build() {
            return new Button(this);
        }
    }
}
