package me.chubbyduck.canvas.elements.control;

import java.util.function.Consumer;
import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.Sprite;
import me.chubbyduck.canvas.core.atlas.StyledText;
import me.chubbyduck.canvas.elements.skin.OreSkins;
import me.chubbyduck.canvas.layout.LayoutContext;
import me.chubbyduck.canvas.layout.Rect;
import me.chubbyduck.canvas.layout.Size;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

/**
 * An OreUI switch drawn in the canvas, with an "On"/"Off" label to its left.
 */
@NullMarked
public class Toggle extends Node {

    private static final Component ON_LABEL = Component.text("On");
    private static final Component OFF_LABEL = Component.text("Off");
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static Sprite onSprite;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static Sprite offSprite;
    private final ValueBinding<Boolean> value;
    private final Component onLabel;
    private final Component offLabel;

    private Toggle() {
        this(ValueBinding.controlled(false, value -> {
        }), ON_LABEL, OFF_LABEL);
    }

    protected Toggle(final ValueBinding<Boolean> value, final Component onLabel, final Component offLabel) {
        this.value = value;
        this.onLabel = onLabel;
        this.offLabel = offLabel;
    }

    public static Toggle of(final State<Boolean> state) {
        return new Toggle(ValueBinding.bound(state), ON_LABEL, OFF_LABEL);
    }

    public static Toggle of(final boolean value, final Consumer<Boolean> onChange) {
        return new Toggle(ValueBinding.controlled(value, onChange), ON_LABEL, OFF_LABEL);
    }

    public Toggle labels(final String on, final String off) {
        return this.labels(Component.text(on), Component.text(off));
    }

    public Toggle labels(final Component on, final Component off) {
        return new Toggle(this.value, on, off);
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        onSprite = assets.registerSprite(Key.key("canvas", "toggle/on"), OreSkins.toggle(true));
        offSprite = assets.registerSprite(Key.key("canvas", "toggle/off"), OreSkins.toggle(false));
    }

    @Override
    protected Size measure(final LayoutContext context) {
        final Size sprite = context.spriteSize(offSprite);
        return new Size(this.labelWidth(context) + sprite.width(), sprite.height());
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        final boolean on = this.value.get();
        final Sprite sprite = on ? onSprite : offSprite;
        final Size size = context.spriteSize(sprite);
        final int spriteX = rect.right() - size.width();
        final int spriteY = rect.y() + (rect.height() - size.height()) / 2;
        final Component label = on ? this.onLabel : this.offLabel;
        if (!StyledText.plain(label).isEmpty()) {
            final int width = context.textWidth(label);
            context.text(label, spriteX - 4 - width, spriteY + (size.height() - 8) / 2, context.theme().palette().textMuted());
        }
        context.sprite(sprite, spriteX, spriteY);
        context.hit(rect, () -> this.value.set(!on));
    }

    @Override
    protected void plain(final PlainContext context) {
        final boolean on = this.value.get();
        context.button(
            Component.text().content(on ? "[x] " : "[ ] ").append(on ? this.onLabel : this.offLabel).build(),
            () -> this.value.set(!on)
        );
    }

    private int labelWidth(final LayoutContext context) {
        final int widest = Math.max(context.textWidth(this.onLabel), context.textWidth(this.offLabel));
        return widest == 0 ? 0 : widest + 4;
    }
}
