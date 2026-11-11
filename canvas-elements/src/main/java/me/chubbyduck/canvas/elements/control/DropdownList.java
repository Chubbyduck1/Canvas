package me.chubbyduck.canvas.elements.control;

import java.util.List;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.PaintContext;
import org.jspecify.annotations.NullMarked;

/** The open option list of a {@link Dropdown}. */
@NullMarked
public class DropdownList extends Node {

    /** Click areas are whole 9px text rows, so rows are two text rows tall and start on a row. */
    public static final int ROW_GRID = 9;
    private static final int ROW_HEIGHT = 2 * ROW_GRID;

    private final List<DropdownNode.Option> options;
    private final Runnable close;
    /** Top edge of the dropdown's button, set while laying out. */
    public int anchorTop;

    DropdownList(final List<DropdownNode.Option> options, final Runnable close) {
        this.options = options;
        this.close = close;
    }

    int listHeight() {
        return this.options.size() * ROW_HEIGHT;
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect canvas = context.canvasRect();
        Rect rect = context.rect();
        if (rect.bottom() > canvas.bottom()) {
            // Not enough room below: open upward, ending on a row boundary above the button.
            final int bottom = Math.floorDiv(this.anchorTop - 1, ROW_GRID) * ROW_GRID;
            rect = new Rect(rect.x(), Math.max(0, bottom - rect.height()), rect.width(), rect.height());
        }

        // Clicks anywhere else close the list; the rows' own click areas are painted after this,
        // so they win where they overlap.
        context.hit(canvas, this.close);

        final int textColor = context.theme().palette().text();
        final int onAccent = context.theme().palette().textOnAccent();
        for (int i = 0; i < this.options.size(); i++) {
            final DropdownNode.Option option = this.options.get(i);
            final Rect row = new Rect(rect.x(), rect.y() + i * ROW_HEIGHT, rect.width(), ROW_HEIGHT);
            context.nineSlice(Button.skin(option.selected(), false), row);
            DropdownNode.drawOption(context, option, row.x() + DropdownNode.PADDING, row.y(), row.height(),
                option.selected() ? onAccent : textColor);
            context.hit(row, option.onSelect());
        }
    }
}
