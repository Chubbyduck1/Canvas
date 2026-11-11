package me.chubbyduck.canvas.paint;

import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

/** A native dialog input. */
@NullMarked
public sealed interface DialogInput {

    Component label();

    /** A text box. */
    record Text(
        Component label,
        boolean labelVisible,
        String initial,
        int maxLength,
        int width,
        Consumer<String> onValue
    ) implements DialogInput {
        public Text {
            maxLength = Math.max(1, maxLength);
            width = Math.clamp(width, 1, 1024);
        }
    }

    /** A checkbox (restyled as a toggle by the pack). */
    record Bool(Component label, boolean initial, Consumer<Boolean> onValue) implements DialogInput {
    }

    /** A slider. */
    record Range(
        Component label,
        String labelFormat,
        float start,
        float end,
        float step,
        float initial,
        int width,
        Consumer<Float> onValue
    ) implements DialogInput {
        public Range {
            width = Math.clamp(width, 1, 1024);
        }
    }

    /** A cycling option button. */
    record Options(
        Component label,
        boolean labelVisible,
        List<Option> options,
        String initialId,
        int width,
        Consumer<String> onValue
    ) implements DialogInput {
        public Options {
            options = List.copyOf(options);
            width = Math.clamp(width, 1, 1024);
        }

        public record Option(String id, Component display) {
        }

    }
}
