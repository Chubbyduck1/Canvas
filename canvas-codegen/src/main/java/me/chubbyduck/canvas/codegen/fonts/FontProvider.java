package me.chubbyduck.canvas.codegen.fonts;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * One entry of a font's {@code providers} array, chosen by its {@code type} field.
 */
public sealed interface FontProvider permits BitmapProvider, ReferenceProvider, SpaceProvider, UnihexProvider {

    /**
     * Client options this provider requires, e.g. {@code {"uniform": false}}; {@code null} for none.
     */
    @Nullable Map<String, Boolean> filter();

    /**
     * Whether the provider is active under the given client options (options not given count as {@code false}).
     */
    default boolean matches(final @NonNull Map<String, Boolean> options) {
        final Map<String, Boolean> filter = this.filter();

        if (filter == null) {
            return true;
        }

        for (final Map.Entry<String, Boolean> option : filter.entrySet()) {
            if (options.getOrDefault(option.getKey(), false) != option.getValue().booleanValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Decodes a provider into the record for its {@code type}.
     */
    final class Deserializer implements JsonDeserializer<FontProvider> {

        @Override
        public FontProvider deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final JsonElement type = json.getAsJsonObject().get("type");

            if (type == null) {
                throw new JsonParseException("Font provider without a type: " + json);
            }

            final Class<? extends FontProvider> target = switch (type.getAsString()) {
                case "bitmap" -> BitmapProvider.class;
                case "reference" -> ReferenceProvider.class;
                case "space" -> SpaceProvider.class;
                case "unihex" -> UnihexProvider.class;
                default -> throw new JsonParseException("Unsupported font provider type " + type.getAsString());
            };
            return context.deserialize(json, target);
        }
    }
}
