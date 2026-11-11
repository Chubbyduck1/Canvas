package me.chubbyduck.canvas.paint;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** A native footer button. */
@NullMarked
public record DialogAction(Component label, @Nullable Component tooltip, int width, Runnable onClick) {

    public static DialogAction of(final Component label, final Runnable onClick) {
        return new DialogAction(label, null, 150, onClick);
    }

}
