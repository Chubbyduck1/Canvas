package me.chubbyduck.canvas.elements.input;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.paint.DialogInput;
import me.chubbyduck.canvas.paint.PaintContext;
import me.chubbyduck.canvas.paint.PlainContext;
import me.chubbyduck.canvas.state.State;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

/** Native dialog inputs: a real text box, checkbox, slider or option button. */
@NullMarked
public class NativeInput extends Node {

    private final DialogInput input;

    protected NativeInput(final DialogInput input) {
        this.input = input;
    }

    /** A text box. */
    public static NativeInput text(final String label, final State<String> state) {
        return text(label, state.get(), 256, state::set);
    }

    public static NativeInput text(final Component label, final State<String> state) {
        return text(label, state.get(), 256, state::set);
    }

    public static NativeInput text(final String label, final String initial, final int maxLength, final Consumer<String> onValue) {
        return text(Component.text(label), initial, maxLength, onValue);
    }

    public static NativeInput text(final Component label, final String initial, final int maxLength, final Consumer<String> onValue) {
        return new NativeInput(new DialogInput.Text(label, true, initial, maxLength, 200, onValue));
    }

    /** A checkbox (drawn as a switch by the Canvas pack). */
    public static NativeInput checkbox(final String label, final State<Boolean> state) {
        return checkbox(Component.text(label), state);
    }

    public static NativeInput checkbox(final Component label, final State<Boolean> state) {
        return new NativeInput(new DialogInput.Bool(label, state.get(), state::set));
    }

    /** A native slider between {@code min} and {@code max}. */
    public static NativeInput slider(final String label, final int min, final int max, final int step, final State<Integer> state) {
        return slider(Component.text(label), min, max, step, state);
    }

    public static NativeInput slider(final Component label, final int min, final int max, final int step, final State<Integer> state) {
        return new NativeInput(new DialogInput.Range(
            label,
            "options.generic_value",
            min,
            max,
            Math.max(1, step),
            state.get(),
            200,
            value -> state.set(Math.round(value))
        ));
    }

    /** A cycling option button. */
    public static NativeInput options(final String label, final Map<String, String> options, final State<String> state) {
        final Map<String, Component> display = new java.util.LinkedHashMap<>();
        options.forEach((id, text) -> display.put(id, Component.text(text)));
        return options(Component.text(label), display, state);
    }

    /** A cycling option button with styled label and option text. */
    public static NativeInput options(final Component label, final Map<String, ? extends Component> options, final State<String> state) {
        final List<DialogInput.Options.Option> entries = options.entrySet().stream()
            .map(entry -> new DialogInput.Options.Option(entry.getKey(), entry.getValue()))
            .toList();
        return new NativeInput(new DialogInput.Options(label, true, entries, state.get(), 200, state::set));
    }

    public DialogInput input() {
        return this.input;
    }

    @Override
    protected void paint(final PaintContext context) {
        context.input(this.input);
    }

    @Override
    protected void plain(final PlainContext context) {
        context.input(this.input);
    }
}
