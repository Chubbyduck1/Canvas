package me.chubbyduck.canvas.core.dialog;

import java.util.Map;
import org.jspecify.annotations.NullMarked;

/**
 * A compiled dialog plus the handlers its click payloads refer to.
 */
@NullMarked
public record CompiledDialog(RenderedDialog dialog, Map<String, Runnable> handlers, Map<String, InputBinding> inputs) {
}
