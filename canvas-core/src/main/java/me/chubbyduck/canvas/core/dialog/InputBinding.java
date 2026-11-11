package me.chubbyduck.canvas.core.dialog;

import me.chubbyduck.canvas.paint.DialogInput;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Delivers a player's input value to the {@link DialogInput}'s callback, validating it first. */
@NullMarked
public record InputBinding(DialogInput input) {

    /** Applies the value read from a dialog response. */
    public void apply(final InputReader reader, final String key) {
        switch (this.input) {
            case final DialogInput.Text text -> {
                final String value = reader.text(key);
                if (value == null) {
                    return;
                }
                text.onValue().accept(value.substring(0, Math.min(value.length(), text.maxLength())));
            }
            case final DialogInput.Bool bool -> {
                final Boolean value = reader.bool(key);
                if (value == null) {
                    return;
                }
                bool.onValue().accept(value);
            }
            case final DialogInput.Range range -> {
                final Float value = reader.number(key);
                if (value == null || value.isNaN()) {
                    return;
                }
                final float minimum = Math.min(range.start(), range.end());
                final float maximum = Math.max(range.start(), range.end());
                range.onValue().accept(Math.clamp(value, minimum, maximum));
            }
            case final DialogInput.Options options -> {
                final String value = reader.text(key);
                if (value == null || options.options().stream().noneMatch(option -> option.id().equals(value))) {
                    return;
                }
                options.onValue().accept(value);
            }
        }
    }

    /** Reads values from a dialog response. */
    public interface InputReader {

        @Nullable String text(String key);

        @Nullable Boolean bool(String key);

        @Nullable Float number(String key);

    }
}
