package me.chubbyduck.canvas.paint;

import me.chubbyduck.canvas.asset.NineSlice;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.asset.Theme;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Given to {@link Node#paint}. */
@NullMarked
public interface PaintContext {

    Rect rect();

    Theme theme();

    Rect canvasRect();

    void sprite(Sprite sprite, int x, int y);

    void nineSlice(NineSlice skin, Rect rect);

    /** Draws one line of styled text (newlines are ignored; wrap with the {@code Text} element). */
    void text(Component text, int x, int y);

    /** Draws styled text with a default color for the parts that don't set their own. */
    default void text(final Component text, final int x, final int y, final int color) {
        this.text(Component.text().color(TextColor.color(color)).append(text).build(), x, y);
    }

    /** Shortcut for plain text in one color. */
    default void text(final String text, final int x, final int y, final int color, final boolean bold) {
        this.text(
            Component.text(text, TextColor.color(color)).decoration(TextDecoration.BOLD, bold),
            x,
            y
        );
    }

    int textWidth(Component text);

    default int textWidth(final String text, final boolean bold) {
        return this.textWidth(Component.text(text).decoration(TextDecoration.BOLD, bold));
    }

    Size spriteSize(Sprite sprite);

    /** Makes a rectangle clickable. */
    void hit(Rect rect, Runnable onClick, @Nullable Component tooltip);

    default void hit(final Rect rect, final Runnable onClick) {
        this.hit(rect, onClick, null);
    }

    /** Adds a native dialog input. */
    void input(DialogInput input);

    /** Adds a native button to the dialog footer. */
    void footer(DialogAction action);

}
