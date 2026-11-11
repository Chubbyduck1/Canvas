package me.chubbyduck.canvas.core.render;

import java.util.List;
import me.chubbyduck.canvas.asset.NineSlice;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.paint.DialogAction;
import me.chubbyduck.canvas.paint.DialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The result of rendering a canvas: draw operations, click regions and native widgets, all in absolute canvas pixels.
 */
@NullMarked
public record PaintedFrame(
    int width,
    int height,
    List<DrawOp> ops,
    List<Hit> hits,
    List<DialogInput> inputs,
    List<DialogAction> footers,
    List<String> warnings
) {

    /** A draw operation. */
    public sealed interface DrawOp {

        long order();
    }

    public record SpriteOp(Sprite sprite, int x, int y, long order) implements DrawOp {
    }

    public record NineOp(NineSlice skin, Rect rect, long order) implements DrawOp {
    }

    /** An object component (atlas sprite or player head). */
    public record ObjectOp(net.kyori.adventure.text.object.ObjectContents contents, int x, int y,
                           long order) implements DrawOp {

        public int row() {
            return Math.floorDiv(this.y + 4, 9);
        }
    }

    public record BarOp(int x, int y, int width, int rgb, long order) implements DrawOp {
    }

    /** One run of text in a single style. */
    public record TextOp(String text, int x, int y, Style style, long order) implements DrawOp {

        public boolean bold() {
            return this.style.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE;
        }
    }

    /** A click region. */
    public record Hit(Rect rect, Runnable onClick, @Nullable Component tooltip, long order) {
    }
}
