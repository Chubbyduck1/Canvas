package me.chubbyduck.canvas.elements.display;

import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.NineSlice;
import me.chubbyduck.canvas.elements.skin.OreSkins;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Length;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.PaintContext;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

/** A one-pixel horizontal rule that fills the available width. */
@NullMarked
public class Divider extends Node {

    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static NineSlice ruleSkin;

    private Divider() {
    }

    public static Divider create() {
        final Divider divider = new Divider();
        divider.width(Length.FILL);
        return divider;
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        ruleSkin = assets.registerNineSlice(Key.key("canvas", "divider/rule"), OreSkins.rule());
    }

    @Override
    protected Size measure(final LayoutContext context) {
        return new Size(0, 1);
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        context.nineSlice(ruleSkin, new Rect(rect.x(), rect.y() + rect.height() / 2, rect.width(), 1));
    }
}
