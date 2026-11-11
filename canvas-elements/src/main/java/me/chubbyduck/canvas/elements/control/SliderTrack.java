package me.chubbyduck.canvas.elements.control;

import me.chubbyduck.canvas.asset.AssetRegistrar;
import me.chubbyduck.canvas.asset.NineSlice;
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

/**
 * The track of a glyph {@link Slider}: a sunken well, an accent fill up to the value, and a thumb.
 */
@NullMarked
public class SliderTrack extends Node {

    private static final int HEIGHT = 14;
    private static final int TRACK_HEIGHT = 8;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static NineSlice trackSkin;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static NineSlice fillSkin;
    @SuppressWarnings("NullAway.Init") // set by register() at pack build
    private static Sprite thumbSprite;
    private final ValueBinding<Integer> value;
    private final int min;
    private final int max;
    private final int step;
    private final int trackWidth;

    private SliderTrack() {
        this(ValueBinding.controlled(0, value -> {
        }), 0, 1, 1, 60);
    }

    SliderTrack(final ValueBinding<Integer> value, final int min, final int max, final int step, final int trackWidth) {
        this.value = value;
        this.min = min;
        this.max = Math.max(min + 1, max);
        this.step = Math.max(1, step);
        this.trackWidth = trackWidth;
    }

    @Override
    protected void register(final AssetRegistrar assets) {
        trackSkin = assets.registerNineSlice(Key.key("canvas", "slider_track/track"), OreSkins.sunken());
        fillSkin = assets.registerNineSlice(Key.key("canvas", "slider_track/fill"), OreSkins.sunkenAccent());
        thumbSprite = assets.registerSprite(Key.key("canvas", "slider_track/thumb"), OreSkins.sliderThumb());
    }

    @Override
    protected Size measure(final LayoutContext context) {
        return new Size(this.trackWidth, HEIGHT);
    }

    @Override
    protected void paint(final PaintContext context) {
        final Rect rect = context.rect();
        final int thumbWidth = context.spriteSize(thumbSprite).width();
        final int current = Math.clamp(this.value.get(), this.min, this.max);
        final int trackY = rect.y() + (rect.height() - TRACK_HEIGHT) / 2;
        final Rect track = new Rect(rect.x(), trackY, rect.width(), TRACK_HEIGHT);
        context.nineSlice(trackSkin, track);
        final int thumb = this.thumbX(rect, thumbWidth, current);
        final int fillWidth = thumb + thumbWidth / 2 - rect.x();
        if (fillWidth >= 4) {
            context.nineSlice(fillSkin, new Rect(rect.x(), trackY, fillWidth, TRACK_HEIGHT));
        }
        context.sprite(thumbSprite, thumb, rect.y() + (rect.height() - context.spriteSize(thumbSprite).height()) / 2);

        // One click region per step, split halfway between neighbouring thumb positions.
        final int steps = (this.max - this.min) / this.step;
        int previousEdge = rect.x();
        for (int stepIndex = 0; stepIndex <= steps; stepIndex++) {
            final int value = this.min + stepIndex * this.step;
            final int center = this.thumbX(rect, thumbWidth, value) + thumbWidth / 2;
            final int nextCenter = stepIndex < steps
                ? this.thumbX(rect, thumbWidth, value + this.step) + thumbWidth / 2
                : rect.right() * 2 - center;
            final int edge = stepIndex < steps ? (center + nextCenter) / 2 : rect.right();
            if (edge > previousEdge) {
                context.hit(new Rect(previousEdge, rect.y(), edge - previousEdge, rect.height()), () -> this.value.set(value));
            }
            previousEdge = edge;
        }
    }

    @Override
    protected void plain(final PlainContext context) {
        context.text(Component.text(this.value.get() + " (" + this.min + "-" + this.max + ")"));
    }

    private int thumbX(final Rect rect, final int thumbWidth, final int value) {
        final int travel = rect.width() - thumbWidth;
        return rect.x() + Math.round(travel * (float) (value - this.min) / (this.max - this.min));
    }
}
