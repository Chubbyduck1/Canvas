package me.chubbyduck.canvas.codegen.fonts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * A font JSON file ({@code assets/<namespace>/font/<path>.json}): its providers, in priority order.
 */
public record FontDefinition(List<FontProvider> providers) {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(FontProvider.class, new FontProvider.Deserializer())
        .create();

    public static FontDefinition parse(final String json) {
        return GSON.fromJson(json, FontDefinition.class);
    }

    /**
     * The providers whose filter matches the given client options, in priority order.
     */
    @NonNull
    public List<FontProvider> activeProviders(final @NonNull Map<String, Boolean> options) {
        return this.providers.stream()
            .filter(provider -> provider.matches(options))
            .toList();
    }

}
