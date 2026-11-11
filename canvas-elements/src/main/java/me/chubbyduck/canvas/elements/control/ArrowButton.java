package me.chubbyduck.canvas.elements.control;

import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.elements.skin.OreSkins;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

/** A square button with a pixel arrow, used by steppers, sliders, and pagers. */
@NullMarked
public class ArrowButton extends Node {

    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static Sprite leftSprite;

    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static Sprite rightSprite;

    private final boolean left;
    private final Runnable onClick;
    private final boolean disabled;
    private final int size;

    private ArrowButton() {
        this(true, () -> {
        }, false, 20);
    }

    protected ArrowButton(final boolean left, final Runnable onClick, final boolean disabled, final int size) {
        this.left = left;
        this.onClick = onClick;
        this.disabled = disabled;
        this.size = size;
    }

    public static ArrowButton left(final Runnable onClick) {
        return new ArrowButton(true, onClick, false, 20);
    }

    public static ArrowButton right(final Runnable onClick) {
        return new ArrowButton(false, onClick, false, 20);
    }

    static ArrowButton step(final ValueBinding<Integer> value, final int current, final int boundary, final int step) {
        final boolean left = step < 0;
        final int next = left
            ? (int) Math.max(boundary, (long) current + step)
            : (int) Math.min(boundary, (long) current + step);
        return new ArrowButton(left, () -> value.set(next), left ? current <= boundary : current >= boundary, 20);
    }

    public ArrowButton disabled(final boolean disabled) {
        return new ArrowButton(this.left, this.onClick, disabled, this.size);
    }

    public ArrowButton sized(final int size) {
        return new ArrowButton(this.left, this.onClick, this.disabled, size);
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        leftSprite = assets.registerSprite(Key.key("canvas", "arrow_button/left"), OreSkins.arrow(true));
        rightSprite = assets.registerSprite(Key.key("canvas", "arrow_button/right"), OreSkins.arrow(false));
    }

    @Override
    protected Size measure(final LayoutContext context) {
        return new Size(this.size, this.size);
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        context.nineSlice(Button.skin(false, this.disabled), rect);
        final Sprite arrow = this.left ? leftSprite : rightSprite;
        final Size size = context.spriteSize(arrow);
        context.sprite(arrow, rect.x() + (rect.width() - size.width()) / 2, rect.y() + OreSkins.buttonContentY(rect.height(), size.height()));
        if (!this.disabled) {
            context.hit(rect, this.onClick);
        }
    }

    @Override
    protected void plain(final PlainContext context) {
        if (this.disabled) {
            return;
        }
        context.button(Component.text(this.left ? "<" : ">"), this.onClick);
    }
}
