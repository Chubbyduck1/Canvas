package me.chubbyduck.canvas.kitchensink.pages;

import java.util.LinkedHashMap;
import java.util.Map;
import me.chubbyduck.canvas.elements.control.Toggle;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.SettingRow;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.input.ActionBar;
import me.chubbyduck.canvas.elements.input.NativeInput;
import me.chubbyduck.canvas.node.Node;
import me.chubbyduck.canvas.node.NodeLike;
import me.chubbyduck.canvas.state.Scope;
import me.chubbyduck.canvas.state.State;
import org.jspecify.annotations.NullMarked;

/**
 * Pages for native inputs and how their values reach state.
 */
@NullMarked
public final class InputPages {

    private InputPages() {
    }

    public static class InputSyncPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<String> text = scope.useState("hello");
            final State<Boolean> check = scope.useState(false);
            final State<Integer> number = scope.useState(5);
            final State<String> option = scope.useState("b");
            final Map<String, String> options = new LinkedHashMap<>();
            options.put("a", "Option A");
            options.put("b", "Option B");
            options.put("c", "Option C");
            return PageChrome.page(scope, "Input sync", "Edit the native inputs below, then click Apply: values land in state.",
                Panel.builder()
                    .title("State")
                    .add(Text.of("text = " + text.get()))
                    .add(Text.of("checkbox = " + check.get() + ", slider = " + number.get() + ", option = " + option.get()))
                    .build(),
                NativeInput.text("Text", text),
                NativeInput.checkbox("Checkbox", check),
                NativeInput.slider("Slider", 0, 10, 1, number),
                NativeInput.options("Option", options, option),
                ActionBar.builder().button("Apply", () -> {
                }).build()
            );
        }
    }

    public static class MixedInputsPage implements NodeLike {

        @Override
        public Node getNode(final Scope scope) {
            final State<Boolean> enabled = scope.useState(true);
            return PageChrome.page(scope, "Mixed inputs", "A glyph toggle and a native checkbox bound to the same state.",
                Panel.builder()
                    .title("Enabled: " + enabled.get())
                    .add(SettingRow.builder().label("Glyph toggle").description("Updates immediately.").control(Toggle.of(enabled)).build())
                    .add(Text.muted("The native checkbox below only syncs on Apply. Clicking the glyph toggle first re-renders with the last known value."))
                    .build(),
                NativeInput.checkbox("Native checkbox", enabled),
                ActionBar.builder().button("Apply", () -> {
                }).build()
            );
        }
    }
}
