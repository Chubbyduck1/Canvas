package me.chubbyduck.canvas.pack;

import org.jspecify.annotations.NullMarked;

/**
 * Resource pack metadata and options.
 */
@NullMarked
public record PackOptions(int minFormat, int maxFormat, String description, boolean hideFocusOutline) {

    public static final PackOptions DEFAULT = new PackOptions(63, 97, "Canvas UI", true);

    public PackOptions withHideFocusOutline(final boolean hide) {
        return new PackOptions(this.minFormat, this.maxFormat, this.description, hide);
    }
}
