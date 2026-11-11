package me.chubbyduck.canvas.core.dialog;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import me.chubbyduck.canvas.paint.DialogInput;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** A rendered dialog, ready to show. */
@NullMarked
public record RenderedDialog(
    Component title,
    List<Body> body,
    List<Input> inputs,
    List<Button> buttons,
    int columns,
    boolean closeOnEscape,
    Button exit
) {

    public static final Key ACTION = Key.key("canvas", "act");

    public static final String FIELD_SESSION = "s";
    public static final String FIELD_NONCE = "v";
    public static final String FIELD_ACTION = "a";

    public static final String ACTION_CLOSE = "close";

    public static String payload(final String session, final int nonce, final String action) {
        return "{" + FIELD_SESSION + ":\"" + session + "\"," + FIELD_NONCE + ":" + nonce + ","
            + FIELD_ACTION + ":\"" + action + "\"}";
    }

    public record Body(Component text, int width) {
    }

    public record Button(Component label, @Nullable Component tooltip, int width, String payload) {
    }

    /** A native input; equality and toString skip its callback so unchanged renders stay equal. */
    public record Input(String key, DialogInput input) {

        private List<@Nullable Object> values() {
            final RecordComponent[] components = this.input.getClass().getRecordComponents();
            final List<@Nullable Object> values = new ArrayList<>(components.length);
            for (final RecordComponent component : components) {
                if (component.getType() == Consumer.class) {
                    continue;
                }
                try {
                    values.add(component.getAccessor().invoke(this.input));
                } catch (final ReflectiveOperationException exception) {
                    throw new IllegalStateException(exception);
                }
            }
            return values;
        }

        @Override
        public boolean equals(final @Nullable Object other) {
            return other instanceof final Input that
                && this.key.equals(that.key)
                && this.input.getClass() == that.input.getClass()
                && this.values().equals(that.values());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.key, this.input.getClass(), this.values());
        }

        @Override
        public String toString() {
            return this.input.getClass().getSimpleName() + "[key=" + this.key + ", " + this.values() + "]";
        }
    }
}
