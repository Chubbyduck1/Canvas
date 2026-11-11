package me.chubbyduck.canvas.codegen.fonts;

import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@code "type": "reference"}: includes every provider of another font.
 */
public record ReferenceProvider(@NonNull String id, @Nullable Map<String, Boolean> filter) implements FontProvider {
}
