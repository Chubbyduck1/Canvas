package me.chubbyduck.canvas.elements.control;

import java.util.List;
import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.elements.skin.OreSkins;
import me.chubbyduck.canvas.elements.skin.PixelCanvas;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The drawn part of a {@link Dropdown}: the closed button, plus the option list as a child while open.
 */
@NullMarked
public class DropdownNode extends Node {

    static final int PADDING = 6;
    private static final int HEIGHT = 20;
    private static final int ICON_GAP = 4;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static Sprite chevronDownSprite;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static Sprite chevronUpSprite;
    private final List<Option> options;
    private final @Nullable Option selected;
    private final boolean open;
    private final Runnable toggle;
    private final int fixedWidth;
    private final @Nullable DropdownList list;

    private DropdownNode() {
        this(List.of(), null, false, () -> {
        }, () -> {
        }, 0);
    }

    DropdownNode(
        final List<Option> options,
        final @Nullable Option selected,
        final boolean open,
        final Runnable toggle,
        final Runnable close,
        final int fixedWidth
    ) {
        this.options = options;
        this.selected = selected;
        this.open = open;
        this.toggle = toggle;
        this.fixedWidth = fixedWidth;
        this.list = open ? new DropdownList(options, close) : null;
        if (this.list != null) {
            // The list draws over everything else on the canvas.
            this.list.z(1000);
        }
    }

    private static java.awt.image.BufferedImage chevron(final int color, final boolean up) {
        final String[] down = {"#.....#", ".#...#.", "..#.#..", "...#..."};
        final String[] rows = up ? new String[]{down[3], down[2], down[1], down[0]} : down;
        return PixelCanvas.canvas(7, 4).shape(0, 0, color, rows).image();
    }

    static int contentWidth(final LayoutContext context, final Option option) {
        final int icon = option.icon() == null ? 0 : context.spriteSize(option.icon()).width() + ICON_GAP;
        return icon + context.textWidth(option.label());
    }

    /**
     * Draws an option's icon and label, vertically centered on a button face.
     */
    static void drawOption(final PaintContext context, final Option option, final int x, final int y, final int height, final int color) {
        int cursor = x;
        if (option.icon() != null) {
            final Size size = context.spriteSize(option.icon());
            context.sprite(option.icon(), cursor, y + OreSkins.buttonContentY(height, size.height()));
            cursor += size.width() + ICON_GAP;
        }
        context.text(option.label(), cursor, y + OreSkins.buttonContentY(height, 8), color);
    }

    @Override
    public List<? extends NodeLike> children() {
        return this.list == null ? List.of() : List.of(this.list);
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        chevronDownSprite = assets.registerSprite(Key.key("canvas", "dropdown_node/chevron_down"), theme -> chevron(theme.palette().text(), false));
        chevronUpSprite = assets.registerSprite(Key.key("canvas", "dropdown_node/chevron_up"), theme -> chevron(theme.palette().text(), true));
    }

    @Override
    protected Size measure(final LayoutContext context) {
        if (this.fixedWidth > 0) {
            return new Size(this.fixedWidth, HEIGHT);
        }
        int widest = 0;
        for (final Option option : this.options) {
            widest = Math.max(widest, contentWidth(context, option));
        }
        final int chevron = context.spriteSize(chevronDownSprite).width();
        return new Size(PADDING + widest + PADDING + chevron + PADDING, HEIGHT);
    }

    @Override
    protected void arrange(final Rect rect, final LayoutContext context) {
        if (this.list == null) {
            return;
        }
        final int top = Math.ceilDiv(rect.bottom(), DropdownList.ROW_GRID) * DropdownList.ROW_GRID;
        this.list.anchorTop = rect.y();
        context.children().getFirst().place(new Rect(rect.x(), top, rect.width(), this.list.listHeight()));
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        context.nineSlice(Button.skin(false, false), rect);
        if (this.selected != null) {
            drawOption(context, this.selected, rect.x() + PADDING, rect.y(), rect.height(), context.theme().palette().text());
        }
        final Sprite chevron = this.open ? chevronUpSprite : chevronDownSprite;
        final Size size = context.spriteSize(chevron);
        context.sprite(chevron, rect.right() - PADDING - size.width(), rect.y() + OreSkins.buttonContentY(rect.height(), size.height()));
        context.hit(rect, this.toggle);
    }

    @Override
    protected void plain(final PlainContext context) {
        for (final Option option : this.options) {
            context.button(
                Button.selectedLabel(option.label(), option.selected()),
                option.onSelect()
            );
        }
    }

    /**
     * One option as drawn.
     */
    record Option(Component label, @Nullable Sprite icon, boolean selected, Runnable onSelect) {
    }
}
