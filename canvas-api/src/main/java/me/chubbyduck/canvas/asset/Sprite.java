package me.chubbyduck.canvas.asset;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

/**
 * Handle to a registered image.
 */
@NullMarked
public record Sprite(Key key) {
}
