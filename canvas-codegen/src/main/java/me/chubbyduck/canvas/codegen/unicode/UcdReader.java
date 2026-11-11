package me.chubbyduck.canvas.codegen.unicode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.jspecify.annotations.NonNull;

/**
 * Downloads files of the Unicode Character Database from unicode.org.
 */
public class UcdReader {

    private final HttpClient client;
    private final String version;

    /**
     * @param version a Unicode version such as {@code 16.0.0}, or {@code latest}
     */
    public UcdReader(final @NonNull HttpClient client, final @NonNull String version) {
        this.client = client;
        this.version = version;
    }

    @NonNull
    public ArabicShapingFile arabicShaping() throws IOException, InterruptedException {
        return ArabicShapingFile.parse(this.fetch("ArabicShaping.txt"));
    }

    @NonNull
    public UnicodeDataFile unicodeData() throws IOException, InterruptedException {
        return UnicodeDataFile.parse(this.fetch("UnicodeData.txt"));
    }

    private String fetch(final String file) throws IOException, InterruptedException {
        final String directory = this.version.equals("latest") ? "UCD/latest" : this.version;
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.unicode.org/Public/" + directory + "/ucd/" + file))
            .build();

        final HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " downloading " + request.uri());
        }

        return response.body();
    }

}
