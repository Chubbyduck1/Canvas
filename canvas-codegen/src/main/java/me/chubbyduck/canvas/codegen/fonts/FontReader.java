package me.chubbyduck.canvas.codegen.fonts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.imageio.ImageIO;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Downloads a font and the assets its providers point at, following references the way the client loads them.
 */
public class FontReader {

    private static final String BASE_URL = "https://assets.mcasset.cloud/";
    private static final String VERSION_HEADER = "x-minecraft-version";
    private static final Map<String, Boolean> OPTIONS = Map.of("uniform", false, "jp", false);
    private final HttpClient client;
    private String resolvedVersion;

    public FontReader(final HttpClient client, final String version) {
        this.client = client;
        this.resolvedVersion = version;
    }

    /**
     * Adds every glyph of the font; the first provider defining a codepoint wins.
     */
    public void readFont(final @NonNull String id,
                         final @NonNull Map<Integer, int[]> glyphs) throws IOException, InterruptedException {
        final FontDefinition definition = FontDefinition.parse(
            new String(this.fetch(this.asset(id, "font/", ".json")), StandardCharsets.UTF_8)
        );

        for (final FontProvider provider : definition.activeProviders(OPTIONS)) {
            final Map<Integer, int[]> own = switch (provider) {
                case ReferenceProvider reference -> {
                    this.readFont(reference.id(), glyphs);
                    yield Map.of();
                }
                case SpaceProvider space -> space.glyphs();
                case BitmapProvider bitmap -> bitmap.glyphs(ImageIO.read(new ByteArrayInputStream(
                    this.fetch(this.asset(bitmap.file(), "textures/", "")))
                ));
                case UnihexProvider unihex -> unihex.glyphs(this.fetch(this.asset(unihex.hexFile(), "", "")));
            };
            own.forEach(glyphs::putIfAbsent);
        }
    }

    /**
     * {@code namespace:path} to {@code namespace/<prefix>path<suffix>}.
     */
    private String asset(final String id, final String prefix, final String suffix) {
        final int colon = id.indexOf(':');
        final String namespace = colon < 0 ? "minecraft" : id.substring(0, colon);
        return namespace + "/" + prefix + id.substring(colon + 1) + suffix;
    }

    /**
     * Fetches an asset; a {@code latest} request is pinned to the version the first response reports.
     */
    private byte[] fetch(final String asset) throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + this.resolvedVersion + "/assets/" + asset))
            .build();

        final HttpResponse<byte[]> response = this.client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " downloading " + request.uri());
        }

        if (this.resolvedVersion.equals("latest")) {
            this.resolvedVersion = response.headers().firstValue(VERSION_HEADER)
                .orElseThrow(() -> new IOException("No " + VERSION_HEADER + " header on " + request.uri()));
        }

        return response.body();
    }

    @Nullable
    public String getResolvedVersion() {
        return this.resolvedVersion;
    }

}
