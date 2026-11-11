package me.chubbyduck.canvas.session;

import org.jspecify.annotations.NullMarked;

/** How {@link me.chubbyduck.canvas.Canvas#open} treats state retained from a previous opening of the same canvas. */
@NullMarked
public enum OpenMode {

    /** Restore the state the player left the canvas with. */
    RESUME,
    /** Discard the retained state and start over. */
    FRESH

}
