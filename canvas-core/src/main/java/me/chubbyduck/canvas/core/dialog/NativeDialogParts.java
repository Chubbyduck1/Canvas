package me.chubbyduck.canvas.core.dialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.chubbyduck.canvas.paint.DialogAction;
import me.chubbyduck.canvas.paint.DialogInput;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Collects the native parts of a dialog (inputs, footer buttons, handlers) shared by the glyph and plain compilers.
 */
@NullMarked
public class NativeDialogParts {

    public static final String INPUT_PREFIX = "canvas_i";

    private final List<RenderedDialog.Input> inputs = new ArrayList<>();
    private final List<RenderedDialog.Button> buttons = new ArrayList<>();
    private final Map<String, Runnable> handlers = new LinkedHashMap<>();
    private final Map<String, InputBinding> bindings = new LinkedHashMap<>();
    private final String session;
    private final int nonce;
    private int nextHandler;

    public NativeDialogParts(final String session, final int nonce) {
        this.session = session;
        this.nonce = nonce;
    }

    /**
     * Registers a handler and returns the SNBT payload that triggers it.
     */
    public String handler(final Runnable handler) {
        final String id = "h" + this.nextHandler++;
        this.handlers.put(id, handler);
        return RenderedDialog.payload(this.session, this.nonce, id);
    }

    public String closePayload() {
        return RenderedDialog.payload(this.session, this.nonce, RenderedDialog.ACTION_CLOSE);
    }

    public void button(final Component label, final @Nullable Component tooltip, final int width, final Runnable onClick) {
        this.buttons.add(new RenderedDialog.Button(label, tooltip, Math.clamp(width, 1, 1024), this.handler(onClick)));
    }

    public void footer(final DialogAction action) {
        this.button(action.label(), action.tooltip(), action.width(), action.onClick());
    }

    public void input(final DialogInput input) {
        final String key = INPUT_PREFIX + this.inputs.size();
        this.bindings.put(key, new InputBinding(input));
        this.inputs.add(new RenderedDialog.Input(key, input));
    }


    public RenderedDialog.Button exitButton() {
        return new RenderedDialog.Button(Component.translatable("gui.done"), null, 150, this.closePayload());
    }

    public CompiledDialog finish(
        final Component title,
        final List<RenderedDialog.Body> body,
        final int columns,
        final boolean closeOnEscape
    ) {
        final RenderedDialog dialog = new RenderedDialog(
            title,
            List.copyOf(body),
            List.copyOf(this.inputs),
            List.copyOf(this.buttons),
            Math.max(1, columns),
            closeOnEscape,
            this.exitButton()
        );
        return new CompiledDialog(dialog, Map.copyOf(this.handlers), Map.copyOf(this.bindings));
    }
}
